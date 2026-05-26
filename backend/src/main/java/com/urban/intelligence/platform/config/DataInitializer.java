package com.urban.intelligence.platform.config;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer - seeds default users and reference data on first startup.
 *
 * Creates the admin user if no admin exists yet.
 * The admin credentials can be overridden via environment variables:
 *   - ADMIN_USERNAME (default: admin)
 *   - ADMIN_EMAIL    (default: admin@urbanplatform.com)
 *   - ADMIN_PASSWORD (default: admin123)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(String... args) {
        // Only seed if no admin user exists (idempotent)
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("INIT: admin user already exists, skipping seed");
            return;
        }

        String adminUsername = environment.getProperty("ADMIN_USERNAME", "admin");
        String adminEmail = environment.getProperty("ADMIN_EMAIL", "admin@urbanplatform.com");
        String adminPassword = environment.getProperty("ADMIN_PASSWORD", "admin123");

        User admin = User.builder()
            .username(adminUsername.toLowerCase().trim())
            .email(adminEmail.toLowerCase().trim())
            .password(passwordEncoder.encode(adminPassword))
            .displayName("Platform Administrator")
            .role(Role.ADMIN)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .emailVerified(true)
            .failedLoginAttempts(0)
            .build();

        userRepository.save(admin);

        log.info("INIT: created admin user '{}' (email={}, role=ADMIN)", admin.getUsername(), admin.getEmail());
        log.info("INIT: admin credentials can be overridden via ADMIN_USERNAME / ADMIN_EMAIL / ADMIN_PASSWORD env vars");
    }
}