package com.eh.springbootcache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedisCacheExpireConfigProperties {
    private final Map<String, Duration> cacheExpire = new HashMap<>();
}
