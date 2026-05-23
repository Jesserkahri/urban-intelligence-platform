package com.urban.intelligence.platform.config;

import io.jsonwebtoken.security.Keys;
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
        initializeFromEnvironment();
    }

    /**
     * Initialize JWT secret from environment variables.
     * Fails fast in production if JWT_SECRET is not configured.
     */
    private void initializeFromEnvironment() {
        // Try to read JWT_SECRET from environment variables
        String envSecret = environment.getProperty("JWT_SECRET");
        
        if (envSecret != null && !envSecret.isBlank()) {
            this.secret = envSecret;
        } else {
            // Check if we're in production
            String[] activeProfiles = environment.getActiveProfiles();
            boolean isProduction = false;
            for (String profile : activeProfiles) {
                if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                    isProduction = true;
                    break;
                }
            }

            if (isProduction) {
                throw new IllegalStateException(
                    "JWT_SECRET environment variable is required for production deployment. " +
                    "Set JWT_SECRET with a cryptographically secure 256-bit key. " +
                    "Example: openssl rand -base64 32"
                );
            }
            
            // Use a temporary dev secret (should not be used in prod)
            this.secret = "dev-secret-key-change-in-production-xxxxxxxxxxxxxxxx";
        }

        // Read optional JWT expiration times
        String expirationMs = environment.getProperty("JWT_EXPIRATION_MS");
        if (expirationMs != null && !expirationMs.isBlank()) {
            try {
                this.expiration = Long.parseLong(expirationMs);
            } catch (NumberFormatException e) {
                // Keep default
            }
        }

        String refreshExpirationMs = environment.getProperty("JWT_REFRESH_EXPIRATION_MS");
        if (refreshExpirationMs != null && !refreshExpirationMs.isBlank()) {
            try {
                this.refreshExpiration = Long.parseLong(refreshExpirationMs);
            } catch (NumberFormatException e) {
                // Keep default
            }
        }
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