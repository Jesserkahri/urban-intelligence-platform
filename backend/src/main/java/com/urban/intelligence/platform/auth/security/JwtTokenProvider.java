package com.urban.intelligence.platform.auth.security;

import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JwtTokenProvider - JWT token generation and validation for auth flows.
 *
 * Generates access tokens (short-lived) and refresh tokens (long-lived)
 * with proper claims, signing, and validation. Supports token type
 * differentiation via custom claims.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Generate an access token for the authenticated user.
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());

        return Jwts.builder()
            .claims(claims)
            .subject(user.getUsername())
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(jwtProperties.getIssuer())
            .audience().add(jwtProperties.getAudience()).and()
            .id(UUID.randomUUID().toString())
            .signWith(jwtProperties.getSigningKey())
            .compact();
    }

    /**
     * Generate a refresh token for the authenticated user.
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("userId", user.getId());

        return Jwts.builder()
            .claims(claims)
            .subject(user.getUsername())
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(jwtProperties.getIssuer())
            .audience().add(jwtProperties.getAudience()).and()
            .id(UUID.randomUUID().toString())
            .signWith(jwtProperties.getSigningKey())
            .compact();
    }

    /**
     * Validate a JWT token and return its claims.
     *
     * @throws SecurityException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(jwtProperties.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw new SecurityException("Invalid or expired JWT token", e);
        }
    }

    /**
     * Extract username/subject from token.
     */
    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extract user ID from token claims.
     */
    public Long getUserIdFromToken(String token) {
        return validateToken(token).get("userId", Long.class);
    }

    /**
     * Check if token is a refresh token.
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the expiration time from token.
     */
    public Date getExpirationFromToken(String token) {
        return validateToken(token).getExpiration();
    }

    /**
     * Check if token has expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get the access token expiration in milliseconds.
     */
    public Long getAccessTokenExpirationMs() {
        return jwtProperties.getExpiration();
    }

    /**
     * Get the refresh token expiration in milliseconds.
     */
    public Long getRefreshTokenExpirationMs() {
        return jwtProperties.getRefreshExpiration();
    }
}