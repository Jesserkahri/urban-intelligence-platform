package com.urban.intelligence.platform.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LoginRateLimiter - simple in-memory rate limiter for login attempts.
 *
 * Uses a sliding window approach per IP address.
 * Lightweight and suitable for MVP — no external dependencies.
 */
@Component
@Slf4j
public class LoginRateLimiter {

    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    // Max 5 attempts per IP per minute
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;

    /**
     * Check if the given IP is allowed to attempt login.
     */
    public boolean isAllowed(String ip) {
        cleanExpired();
        LoginAttempt attempt = attempts.get(ip);
        if (attempt == null) {
            return true;
        }
        return attempt.count < MAX_ATTEMPTS;
    }

    /**
     * Record a login attempt from the given IP.
     */
    public void recordAttempt(String ip) {
        LoginAttempt attempt = attempts.computeIfAbsent(ip, k -> new LoginAttempt());
        synchronized (attempt) {
            if (Instant.now().toEpochMilli() - attempt.windowStart > WINDOW_MS) {
                attempt.windowStart = Instant.now().toEpochMilli();
                attempt.count = 0;
            }
            attempt.count++;

            if (attempt.count >= MAX_ATTEMPTS) {
                log.warn("Rate limit hit for IP: {} ({} attempts in window)", ip, attempt.count);
            }
        }
    }

    /**
     * Reset rate limit for an IP (e.g., on successful login).
     */
    public void reset(String ip) {
        attempts.remove(ip);
    }

    private void cleanExpired() {
        long now = Instant.now().toEpochMilli();
        attempts.entrySet().removeIf(e ->
            now - e.getValue().windowStart > WINDOW_MS
        );
    }

    private static class LoginAttempt {
        long windowStart = Instant.now().toEpochMilli();
        int count = 0;
    }
}