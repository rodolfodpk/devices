package com.rdpk.device;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
public abstract class AbstractIntegrationTest {
    
    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2")
            .withDatabaseName("devices_test")
            .withUsername("devices")
            .withPassword("devices")
            .withReuse(true) // Reuse container across all test classes
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort())
            .withStartupTimeout(java.time.Duration.ofSeconds(30));
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Use supplier functions to lazily evaluate properties after container starts
        // Testcontainers JUnit extension automatically starts containers
        registry.add("spring.r2dbc.url", () -> {
            String url = postgres.getJdbcUrl();
            return url.replace("jdbc:postgresql://", "r2dbc:postgresql://");
        });
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        
        // Flyway configuration (JDBC)
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
    }
}

