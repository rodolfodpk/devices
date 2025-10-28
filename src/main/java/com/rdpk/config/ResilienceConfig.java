package com.rdpk.config;

import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j Configuration
 * 
 * Resilience4j registries are auto-configured by Spring Boot when
 * resilience4j-spring-boot3 dependency is present.
 * 
 * Configuration is done via application.properties:
 * - resilience4j.circuitbreaker.instances.devices.*
 * - resilience4j.retry.instances.devices.*
 * - resilience4j.timelimiter.instances.devices.*
 * 
 * The registries can be injected directly in repositories as beans:
 * - CircuitBreakerRegistry
 * - RetryRegistry
 * - TimeLimiterRegistry
 */
@Configuration
public class ResilienceConfig {
    // Spring Boot auto-configuration creates the registry beans
    // No manual bean creation needed
}

