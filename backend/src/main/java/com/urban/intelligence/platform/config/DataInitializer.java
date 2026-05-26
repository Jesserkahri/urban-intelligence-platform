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

        // Seed manager test account
        String managerUsername = environment.getProperty("MANAGER_USERNAME", "manager");
        String managerEmail = environment.getProperty("MANAGER_EMAIL", "manager@urbanplatform.com");
        String managerPassword = environment.getProperty("MANAGER_PASSWORD", "manager123");

        if (!userRepository.existsByUsername(managerUsername)) {
            User manager = User.builder()
                .username(managerUsername.toLowerCase().trim())
                .email(managerEmail.toLowerCase().trim())
                .password(passwordEncoder.encode(managerPassword))
                .displayName("Platform Manager")
                .role(Role.MANAGER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
            userRepository.save(manager);
            log.info("INIT: created manager user '{}' (email={}, role=MANAGER)", manager.getUsername(), manager.getEmail());
        }

        // Seed viewer test account
        String viewerUsername = environment.getProperty("VIEWER_USERNAME", "viewer");
        String viewerEmail = environment.getProperty("VIEWER_EMAIL", "viewer@urbanplatform.com");
        String viewerPassword = environment.getProperty("VIEWER_PASSWORD", "viewer123");

        if (!userRepository.existsByUsername(viewerUsername)) {
            User viewer = User.builder()
                .username(viewerUsername.toLowerCase().trim())
                .email(viewerEmail.toLowerCase().trim())
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