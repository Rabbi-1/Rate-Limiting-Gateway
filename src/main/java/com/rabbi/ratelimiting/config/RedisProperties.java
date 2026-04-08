package com.rabbi.ratelimiting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
    private String host = "localhost";
    private int port = 6379;
    private int timeout = 2000;

    public JedisPool getJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);// max total connections
        poolConfig.setMaxIdle(10);// idle connections allowed
        poolConfig.setMinIdle(5);// keep at least 5 ready
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        return new JedisPool(poolConfig, host, port, timeout);
        //23;31
    }
}


//spring.data.redis.host=localhost
//spring.redit.port = 6379
//spring.data.redis.timeout=2000