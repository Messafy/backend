package com.luispiquinrey.backend.account.infrastructure.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final long MAX_BUCKET_IDLE_NANOS = Duration.ofMinutes(1).toNanos();
    private final ConcurrentMap<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    private Bucket getBucket(String clientIp) {
        long now = System.nanoTime();
        buckets.entrySet().removeIf(entry -> now - entry.getValue().lastAccessNanos() > MAX_BUCKET_IDLE_NANOS);
        return buckets.compute(clientIp, (ip, existing) ->
                new ClientBucket(existing == null ? createBucket() : existing.bucket(), now)).bucket();
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(10).refillIntervally(1, Duration.ofSeconds(1)))
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Bucket bucket = getBucket(request.getRemoteAddr());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("RateLimit-Limit", "10");
            response.setHeader("RateLimit-Remaining", Long.toString(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"title\":\"Too Many Requests\",\"status\":429}");
    }

    private record ClientBucket(Bucket bucket, long lastAccessNanos) {}
}
