package com.urban.intelligence.platform.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JwtProperties - Configuration properties for JWT token generation and validation
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret = "urban-intelligence-platform-secure-secret-key-min-256-bits";
    private Long expiration = 86400000L; // 24 hours in milliseconds
    private String issuer = "urban-intelligence-platform";
    private String audience = "urban-platform-client";

    /**
     * Get the signing key for JWT operations
     */
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
