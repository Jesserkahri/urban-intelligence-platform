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
        // Seed admin user if needed, otherwise keep existing admin and still seed test accounts.
        String adminUsername = environment.getProperty("ADMIN_USERNAME", "admin");
        String adminEmail = environment.getProperty("ADMIN_EMAIL", "admin@urbanplatform.com");
        String adminPassword = environment.getProperty("ADMIN_PASSWORD", "admin123");

        String normalizedAdminUsername = adminUsername.toLowerCase().trim();
        String normalizedAdminEmail = adminEmail.toLowerCase().trim();

        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = User.builder()
                .username(normalizedAdminUsername)
                .email(normalizedAdminEmail)
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
        } else {
            log.info("INIT: admin user already exists, skipping admin seed");
        }

        // Seed operator test account
        String operatorUsername = environment.getProperty("OPERATOR_USERNAME", "operator");
        String operatorEmail = environment.getProperty("OPERATOR_EMAIL", "operator@urbanplatform.com");
        String operatorPassword = environment.getProperty("OPERATOR_PASSWORD", "operator123");
        String normalizedOperatorUsername = operatorUsername.toLowerCase().trim();
        String normalizedOperatorEmail = operatorEmail.toLowerCase().trim();

        if (!userRepository.existsByUsername(normalizedOperatorUsername)) {
            User operator = User.builder()
                .username(normalizedOperatorUsername)
                .email(normalizedOperatorEmail)
                .password(passwordEncoder.encode(operatorPassword))
                .displayName("Platform Operator")
                .role(Role.OPERATOR)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
            userRepository.save(operator);
            log.info("INIT: created operator user '{}' (email={}, role=OPERATOR)", operator.getUsername(), operator.getEmail());
        }

        // Seed analyst test account
        String analystUsername = environment.getProperty("ANALYST_USERNAME", "analyst");
        String analystEmail = environment.getProperty("ANALYST_EMAIL", "analyst@urbanplatform.com");
        String analystPassword = environment.getProperty("ANALYST_PASSWORD", "analyst123");
        String normalizedAnalystUsername = analystUsername.toLowerCase().trim();
        String normalizedAnalystEmail = analystEmail.toLowerCase().trim();

        if (!userRepository.existsByUsername(normalizedAnalystUsername)) {
            User analyst = User.builder()
                .username(normalizedAnalystUsername)
                .email(normalizedAnalystEmail)
                .password(passwordEncoder.encode(analystPassword))
                .displayName("Platform Analyst")
                .role(Role.ANALYST)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
            userRepository.save(analyst);
            log.info("INIT: created analyst user '{}' (email={}, role=ANALYST)", analyst.getUsername(), analyst.getEmail());
        }

        // Seed viewer test account
        String viewerUsername = environment.getProperty("VIEWER_USERNAME", "viewer");
        String viewerEmail = environment.getProperty("VIEWER_EMAIL", "viewer@urbanplatform.com");
        String viewerPassword = environment.getProperty("VIEWER_PASSWORD", "viewer123");
        String normalizedViewerUsername = viewerUsername.toLowerCase().trim();
        String normalizedViewerEmail = viewerEmail.toLowerCase().trim();

        if (!userRepository.existsByUsername(normalizedViewerUsername)) {
            User viewer = User.builder()
                .username(normalizedViewerUsername)
                .email(normalizedViewerEmail)
                .password(passwordEncoder.encode(viewerPassword))
                .displayName("Platform Viewer")
                .role(Role.VIEWER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
            userRepository.save(viewer);
            log.info("INIT: created viewer user '{}' (email={}, role=VIEWER)", viewer.getUsername(), viewer.getEmail());
        }

        log.info("INIT: credentials can be overridden via *_USERNAME / *_EMAIL / *_PASSWORD env vars");
    }
}