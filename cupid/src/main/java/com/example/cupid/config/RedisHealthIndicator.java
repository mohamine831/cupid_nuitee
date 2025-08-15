package com.example.cupid.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.data.redis.host", havingValue = "localhost")
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            String pong = connection.ping();
            connection.close();
            
            if ("PONG".equals(pong)) {
                log.debug("Redis health check passed");
                return Health.up()
                        .withDetail("redis", "Redis is running")
                        .withDetail("ping", pong)
                        .build();
            } else {
                log.warn("Redis health check failed - unexpected response: {}", pong);
                return Health.down()
                        .withDetail("redis", "Redis is not responding correctly")
                        .withDetail("ping", pong)
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                    .withDetail("redis", "Redis connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
