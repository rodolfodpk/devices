package com.rdpk.device.config;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton container for shared PostgreSQL testcontainer across all integration tests.
 * 
 * This ensures that all test classes share a single PostgreSQL container,
 * eliminating resource contention and port conflicts when running the full test suite.
 * 
 * The container is started once and reused across all test classes, with proper
 * database cleanup via DELETE in each test's @BeforeEach method.
 */
public class SharedPostgresContainer {
    
    private static PostgreSQLContainer<?> container;
    
    /**
     * Get the singleton PostgreSQL container instance.
     * Creates the container on first access if it doesn't exist.
     * 
     * @return the shared PostgreSQL container
     */
    @SuppressWarnings("resource") // Container is intentionally not closed - it's reused across all tests
    public static PostgreSQLContainer<?> getInstance() {
        if (container == null) {
            container = new PostgreSQLContainer<>("postgres:17.2")
                    .withDatabaseName("devices_test")
                    .withUsername("devices")
                    .withPassword("devices")
                    .withReuse(true);
        }
        return container;
    }
    
    /**
     * Start the shared container.
     * This method ensures the container is started before any tests run.
     */
    public static void start() {
        getInstance().start();
    }
}

