package com.urban.intelligence.platform.config;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JwtProperties - Configuration properties for JWT token generation and validation.
 *
 * Reads from environment variables (JWT_SECRET, JWT_EXPIRATION_MS, JWT_REFRESH_EXPIRATION_MS).
 * Fails fast if JWT_SECRET is not configured in production profiles.
 *
 * Environment variables take precedence over defaults.
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private Long expiration = 900000L; // 15 minutes in milliseconds
    private Long refreshExpiration = 604800000L; // 7 days in milliseconds
    private String issuer = "urban-intelligence-platform";
    private String audience = "urban-platform-client";

    private final Environment environment;

    public JwtProperties(Environment environment) {
        this.environment = environment;
    }

    /**
     * Validate bound JWT configuration after Spring has applied environment
     * variables such as JWT_SECRET or APP_JWT_SECRET.
     */
    @PostConstruct
    void validateConfiguration() {
        if (secret != null && !secret.isBlank()) {
            return;
        }

        boolean isProduction = false;
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                isProduction = true;
                break;
            }
        }

        if (isProduction) {
            throw new IllegalStateException(
                "JWT secret is required for production deployment. " +
                "Set JWT_SECRET or APP_JWT_SECRET with a cryptographically secure 256-bit key. " +
                "Example: openssl rand -base64 32"
            );
        }

        // Non-production fallback only; application.properties has no hardcoded prod secret.
        this.secret = "dev-secret-key-change-in-production-xxxxxxxxxxxxxxxx";
    }

    /**
     * Get the signing key for JWT operations.
     * The secret must be at least 256 bits (32 bytes) for HS256.
     */
    public SecretKey getSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT secret is not configured. Set JWT_SECRET environment variable."
            );
        }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
