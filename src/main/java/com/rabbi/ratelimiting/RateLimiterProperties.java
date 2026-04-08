package com.rabbi.ratelimiting;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {
    private long capacity = 10; //Defines burst size (how many requests allowed instantly)
    private long refillRate = 5; //Tokens added per second

    private String apiServerUrl = "http://localhost:8080";
    private int timeout = 5000;
}


//rate-limiter.capacity = 10
//rate-limiter.refill-rate = 5
//rate-limiter.api-server-url = http://localhost:8080
//rate-limiter.timeout=5000

