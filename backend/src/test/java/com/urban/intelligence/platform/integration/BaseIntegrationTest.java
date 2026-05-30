package com.urban.intelligence.platform.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all PostgreSQL-backed integration tests.
 * Uses H2 in-memory database (no Docker required).
 * Testcontainers support is commented out — re-enable if PostgreSQL-specific
 * features are needed and Docker is available.
 *
 * For Docker-based testing, uncomment the Testcontainers section and
 * update application-test.properties to use PostgreSQL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // ---- H2 in-memory (default, no Docker needed) ----
    // The datasource is configured in src/test/resources/application-test.properties
    // which uses jdbc:h2:mem:testdb with PostgreSQL compatibility mode.

    // ---- Testcontainers PostgreSQL (requires Docker) ----
    // Uncomment below if you need real PostgreSQL features:
    //
    // @SuppressWarnings("resource")
    // static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    //     .withDatabaseName("urban_test")
    //     .withUsername("test")
    //     .withPassword("test")
    //     .withReuse(true);
    //
    // static {
    //     postgres.start();
    // }
    //
    // @DynamicPropertySource
    // static void configureProperties(DynamicPropertyRegistry registry) {
    //     registry.add("spring.datasource.url", postgres::getJdbcUrl);
    //     registry.add("spring.datasource.username", postgres::getUsername);
    //     registry.add("spring.datasource.password", postgres::getPassword);
    //     registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    //     registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    //     registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    //     registry.add("spring.flyway.enabled", () -> "true");
    //     registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    //     registry.add("spring.jpa.show-sql", () -> "true");
    //     registry.add("spring.jpa.open-in-view", () -> "false");
    // }
}