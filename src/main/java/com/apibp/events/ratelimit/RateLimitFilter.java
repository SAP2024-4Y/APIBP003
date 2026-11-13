package com.apibp.events.ratelimit;

import com.apibp.events.exception.ApiError;
import com.apibp.events.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final long capacity;
    private final long refillTokens;
    private final long refillSeconds;
    private final boolean enabled;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;

    public RateLimitFilter(
            @Value("${app.rate-limit.capacity:100}") long capacity,
            @Value("${app.rate-limit.refill-tokens:100}") long refillTokens,
            @Value("${app.rate-limit.refill-seconds:60}") long refillSeconds,
            @Value("${app.rate-limit.enabled:true}") boolean enabled,
            ObjectMapper mapper) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillSeconds = refillSeconds;
        this.enabled = enabled;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (!enabled) {
            chain.doFilter(req, res);
            return;
        }

        String key = req.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        var probe = bucket.tryConsumeAndReturnRemaining(1);
        long remaining = probe.getRemainingTokens();
        long resetEpochSeconds = System.currentTimeMillis() / 1000
                + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

        res.setHeader("RateLimit-Limit", String.valueOf(capacity));
        res.setHeader("RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
        res.setHeader("RateLimit-Reset", String.valueOf(resetEpochSeconds));

        if (!probe.isConsumed()) {
            res.setHeader("Retry-After", String.valueOf(refillSeconds));
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(res.getWriter(),
                    new ApiError(ErrorCode.RATE_LIMITED.name(), "Too many requests — slow down"));
            return;
        }

        chain.doFilter(req, res);
    }

    public void resetBuckets() {
        buckets.clear();
    }

    private Bucket newBucket() {
        // ponytail: per-IP in-memory buckets; replace with distributed store (Redis) if horizontal scaling needed
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(refillTokens, Duration.ofSeconds(refillSeconds))
                        .build())
                .build();
    }
}
