package com.eh.springbootcache;

import com.eh.springbootcache.orm.bean.Employee;
import com.eh.springbootcache.orm.dao.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class SpringbootCacheApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(SpringbootCacheApplicationTests.class);

    @Autowired
    private EmployeeMapper employeeMapper;
    /*
    注意这里如果使用@Autowired注解就不能使用泛型，因为自动装配时使用了泛型注入，RedisTemplate<Object, Object>，
    与RedisTemplate<String, Employee>类型不一样，所以要么保持一致，要么使用名称@Resource进行注入
     */
    @Resource
    private RedisTemplate<String, Employee> redisTemplate;


    @Test
    void contextLoads() {
        Employee employee = employeeMapper.selectByPrimaryKey(1);
        redisTemplate.opsForValue().set("emp", employee);
        Employee employee1 = redisTemplate.opsForValue().get("emp");
        logger.info("emp:{}", employee1);
    }

}
