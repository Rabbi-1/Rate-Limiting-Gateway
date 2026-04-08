package com.rabbi.ratelimiting.service;

import com.rabbi.ratelimiting.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@RequiredArgsConstructor
public class RedisTokenBucketService {
    private final JedisPool jedisPool;
    private final RateLimiterProperties properties;

    private final String TOKENS_KEY_PREFIX = "rate_limiter:tokens:";
    private static final String LAST_REFILL_KEY_PREFIX = "rate_limiter:last_refill:";

    public boolean isAllowed(String clientId) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;
        try(Jedis jedis = jedisPool.getResource()) {
            refillTokens(clientId, jedis);
            String tokenStr = jedis.get(tokenKey);
            long currentTokens = tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();

            if(currentTokens <= 0) {
                return false; //the request is rejected immediately
            }
            long decremented = jedis.decr(tokenKey); //decrements the token count in Redis
            return decremented >= 0;
        }
    }
    public long getCapacity(String clientId) {
        return properties.getCapacity();
    }
    public long getAvailableTokens(String clientId) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;
        try(Jedis jedis = jedisPool.getResource()) {
            refillTokens(clientId, jedis);
            String tokenStr = jedis.get(tokenKey);
            return tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();
        }
    }
    public void refillTokens(String clientId, Jedis jedis) {
        //this function ensures that tokens are gradually
        // restored over time, allowing clients to regain
        // the ability to make requests after being rate limited,
        // instead of being permanently blocked.
        //NOTE: Redis itself stores everything as strings
        String tokensKey = TOKENS_KEY_PREFIX + clientId;
        String lastRefillKey = LAST_REFILL_KEY_PREFIX + clientId;

        long now = System.currentTimeMillis();
        String lastRefillStr = jedis.get(lastRefillKey);
        if(lastRefillStr == null){

            jedis.set(tokensKey, String.valueOf(properties.getCapacity()));
            jedis.set(lastRefillKey, String.valueOf(now));
            return;
        }
        long lastRefillTime = Long.parseLong(lastRefillStr);
        long elapsedTime = now - lastRefillTime;
        if(elapsedTime <=0) {
            return;
        }
        long tokensToAdd = (elapsedTime * properties.getRefillRate()) / 1000;
        if(tokensToAdd <= 0) {
            return;
        }
    }
}









