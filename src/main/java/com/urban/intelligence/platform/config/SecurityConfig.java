package com.urban.intelligence.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SecurityConfig - Security framework configuration
 * 
 * Configures password encoding and provides foundation for JWT-based authentication.
 * Prepared for future integration with OAuth2/OIDC and advanced API security patterns.
 */
@Configuration
public class SecurityConfig {

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
