package com.urban.intelligence.platform.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtTokenProvider - Utility component for JWT token generation and validation
 * 
 * Handles creation and validation of JWT tokens for API authentication.
 * Foundation for future OAuth2/OIDC integration and AI/ML API access control.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Generate a JWT token for a user/service
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        log.debug("Generating JWT token for subject: {}", subject);
        
        if (claims == null) {
            claims = new HashMap<>();
        }
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        
return Jwts.builder()
    .claims(claims)                          // replaces .setClaims(claims)
    .subject(subject)                        // replaces .setSubject(subject)
    .issuedAt(now)                           // replaces .setIssuedAt(now)
    .expiration(expiryDate)                  // replaces .setExpiration(expiryDate)
    .issuer(jwtProperties.getIssuer())       // replaces .setIssuer(...)
    .audience().add(jwtProperties.getAudience()).and()  // replaces .setAudience(...)
    .signWith(jwtProperties.getSigningKey()) // SignatureAlgorithm arg dropped — key type infers it
    .compact();
    }

    /**
     * Generate a token with default claims
     */
    public String generateToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "platform");
        return generateToken(subject, claims);
    }

    /**
     * Generate token for analytics/ML pipeline access
     */
    public String generateAnalyticsToken(String analyticsServiceId) {
        log.debug("Generating analytics service token for: {}", analyticsServiceId);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "analytics");
        claims.put("scope", "analytics:read,analytics:write");
        
        return generateToken(analyticsServiceId, claims);
    }

    /**
     * Get token expiration time in milliseconds
     */
    public Long getTokenExpirationMs() {
        return jwtProperties.getExpiration();
    }
}
