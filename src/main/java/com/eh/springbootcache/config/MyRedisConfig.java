package com.eh.springbootcache.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableConfigurationProperties(RedisCacheExpireConfigProperties.class)
public class MyRedisConfig {

    private static final String SYSTEM_CACHE_REDIS_KEY_PREFIX = "springboot-demo";

    @Autowired
    private RedisCacheExpireConfigProperties redisCacheExpireConfigProperties;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 我们为了自己开发方便，一般直接使用 <String, Object>
        // 两个泛型都是 Object, Object 的类型，我们后使用需要强制转换 <String, Object>
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        setRedisTemplate(template);
        template.afterPropertiesSet();
        return template;
    }

    private void setRedisTemplate(RedisTemplate<String, Object> template) {
        // Json序列化配置
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 解决jackson2无法反序列化LocalDateTime的问题
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());

        // 该方法过时
        // om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        // 上面 enableDefaultTyping 方法过时，使用 activateDefaultTyping
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // String 的序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        // 设置值（value）的序列化采用FastJsonRedisSerializer。
        // 设置键（key）的序列化采用StringRedisSerializer。
        template.afterPropertiesSet();
    }


    /**
     * 自定义spring缓存抽象，更好地支持使用JCache（JSR-107）注解简化我们开发
     * 2.x废弃了1.x采用的使用RedisTemplate作为参数构造CacheManager，CacheManager与RedisTemplate无关
     * 默认使用CacheKeyPrefix cacheName -> cacheName::key
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, CacheKeyPrefix cacheKeyPrefix) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(om);


        // 配置序列化（解决乱码的问题）,过期时间30秒
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues()
                .computePrefixWith(cacheKeyPrefix); // 使用自定义cache前缀

        // 给各个cache设置过期时间
        Set<String> cacheNames = new HashSet<>();
        Map<String, RedisCacheConfiguration> cacheExpireConfig = new ConcurrentHashMap<>();
        for (Map.Entry<String, Duration> entry : redisCacheExpireConfigProperties.getCacheExpire().entrySet()) {
            cacheNames.add(entry.getKey());
            cacheExpireConfig.put(entry.getKey(), config.entryTtl(entry.getValue()));
        }


        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .initialCacheNames(cacheNames)
                .withInitialCacheConfigurations(cacheExpireConfig)
                .build();
        return cacheManager;
    }

    @Bean
    public CacheKeyPrefix cacheKeyPrefix() {
        return cacheName -> {
            StringBuilder sb = new StringBuilder();
            sb.append(SYSTEM_CACHE_REDIS_KEY_PREFIX).append(":").append(cacheName).append(":");
            return sb.toString();
        };
    }


}
