package com.rdpk.device;

import com.rdpk.device.config.SharedPostgresContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Abstract base class for all integration tests.
 * 
 * Provides:
 * - Shared PostgreSQL Testcontainer for isolated database testing
 * - Automatic database cleanup before each test
 * - Auto-configured WebTestClient setup
 * 
 * NOTE: Tests use real PostgreSQL via Testcontainers - NO MOCKS
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {
    
    // NOTE: Using SharedPostgresContainer singleton for container reuse
    // Using @TestInstance(PER_CLASS) like vote-system for better performance

    protected static PostgreSQLContainer<?> postgres = SharedPostgresContainer.getInstance();

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Container is already started in static block
        // R2DBC configuration for reactive database access
        String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s",
            postgres.getHost(),
            postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            postgres.getDatabaseName()
        );
        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        
        // Flyway configuration for migrations (uses JDBC)
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
    }

    @Autowired
    protected DatabaseClient databaseClient;
    
    @Autowired  
    protected WebTestClient webTestClient; // Auto-configured by Spring Boot Test

    @BeforeEach
    protected void setUp() {
        // Clear all tables before each test to ensure isolation
        // Use TRUNCATE for faster, more reliable cleanup with automatic sequence reset
        databaseClient.sql("TRUNCATE TABLE devices RESTART IDENTITY CASCADE")
                .fetch()
                .rowsUpdated()
                .block();
    }
}

