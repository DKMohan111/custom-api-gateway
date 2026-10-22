
package com.apigateway.service;

import com.apigateway.exception.RateLimiterUnavailableException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setScriptText(
                "local count = redis.call('INCR', KEYS[1]) " +
                        "if count == 1 then " +
                        "redis.call('PEXPIRE', KEYS[1], ARGV[1]) " +
                        "end " +
                        "return count"
        );
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimitResult checkLimit(String username) {
        String key = "rate_limit:" + username;

        try {
            Long count = redisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    List.of(key),
                    String.valueOf(WINDOW.toMillis())
            );

            if (count == null) {
                throw new RateLimiterUnavailableException();
            }

            long remaining = Math.max(0, MAX_REQUESTS - count);

            return new RateLimitResult(
                    count <= MAX_REQUESTS,
                    remaining
            );
        } catch (RedisConnectionFailureException | RedisSystemException exception) {
            throw new RateLimiterUnavailableException();
        }
    }
}
