# Resilience

Circuit Breaker, Retry, and Timeout patterns for database operations using Resilience4j.

## Overview

Database operations are protected with multiple resilience layers to handle transient failures gracefully:

1. **Circuit Breaker**: Opens after failures to prevent cascading failures
2. **Retry**: Automatic retry with exponential backoff
3. **Timeout**: Fail-fast on slow database operations
4. **Layered Timeouts**: Multi-level timeout protection

## Configuration

### Architecture

Resilience4j is applied at the repository layer using **Reactor transformers**:

```java
@Repository
public class DeviceRepositoryImpl implements DeviceRepository {
    
    private <T> Mono<T> applyResilience(Mono<T> mono) {
        return mono
            .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("devices")))
            .transformDeferred(RetryOperator.of(retryRegistry.retry("devices")))
            .transformDeferred(TimeLimiterOperator.of(timeLimiterRegistry.timeLimiter("devices")));
    }
}
```

This ensures all database operations are automatically protected.

### Properties

Resilience4j configuration in `application.properties`:

```properties
# Circuit Breaker
resilience4j.circuitbreaker.instances.devices.slidingWindowSize=10
resilience4j.circuitbreaker.instances.devices.minimumNumberOfCalls=5
resilience4j.circuitbreaker.instances.devices.failureRateThreshold=50
resilience4j.circuitbreaker.instances.devices.waitDurationInOpenState=60000

# Retry
resilience4j.retry.instances.devices.maxAttempts=3
resilience4j.retry.instances.devices.waitDuration=1000

# Time Limiter
resilience4j.timelimiter.instances.devices.timeoutDuration=5s
```

### K6 Testing Profile

For performance testing, resilience is relaxed in `application-k6.properties`:

```properties
# Relaxed settings for performance testing
resilience4j.circuitbreaker.instances.devices.slidingWindowSize=100
resilience4j.circuitbreaker.instances.devices.failureRateThreshold=80
resilience4j.timelimiter.instances.devices.timeoutDuration=30s
```

## Layered Timeout Strategy

3-layer timeout protection ensures operations fail fast:

```
┌──────────────────────────────────────────┐
│ Resilience4j TimeLimiter (5s)           │
│ Overall operation timeout                │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│ R2DBC Statement Timeout (4s)            │
│ Query execution timeout                  │
└──────────────────┬───────────────────────┘
                   │
┌──────────────────▼───────────────────────┐
│ R2DBC Connection Timeout (3s)           │
│ Database connection timeout              │
└──────────────────────────────────────────┘
```

Each layer fails before triggering the next, providing cascading protection.

### Configuration

```properties
# R2DBC Connection timeout (3s)
spring.r2dbc.properties.connectTimeout=3s

# R2DBC Statement timeout (4s)
spring.r2dbc.properties.statementTimeout=4s

# Resilience4j TimeLimiter (5s)
resilience4j.timelimiter.instances.devices.timeoutDuration=5s
```

**Why Layered?**
- Connection issues fail at 3s (connection layer)
- Slow queries fail at 4s (statement layer)
- Overall operation fails at 5s (Resilience4j layer)

## Circuit Breaker

### Behavior

1. **Closed**: Normal operation, monitoring failures
2. **Open**: Circuit open, returning failure immediately
3. **Half-Open**: Testing if backend recovered

### Configuration

```properties
resilience4j.circuitbreaker.instances.devices.slidingWindowSize=10
resilience4j.circuitbreaker.instances.devices.minimumNumberOfCalls=5
resilience4j.circuitbreaker.instances.devices.failureRateThreshold=50
resilience4j.circuitbreaker.instances.devices.waitDurationInOpenState=60000
```

**Settings:**
- `slidingWindowSize`: Track last 10 calls
- `minimumNumberOfCalls`: Need at least 5 calls before evaluating
- `failureRateThreshold`: Open if 50% failures
- `waitDurationInOpenState`: Wait 60s before trying again

## Retry

### Behavior

Automatically retry failed operations up to 3 times with 1 second delay between attempts.

### Configuration

```properties
resilience4j.retry.instances.devices.maxAttempts=3
resilience4j.retry.instances.devices.waitDuration=1000
```

**Settings:**
- `maxAttempts`: Try 3 times total (initial + 2 retries)
- `waitDuration`: Wait 1 second between retries

## Time Limiter

### Behavior

Kill operations that exceed 5 seconds, preventing slow queries from hanging the system.

### Configuration

```properties
resilience4j.timelimiter.instances.devices.timeoutDuration=5s
```

## Observability

### Metrics

Resilience4j metrics are exposed via Spring Boot Actuator:

```bash
# View Resilience4j metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls

# Circuit breaker states
curl http://localhost:8080/actuator/circuitbreakers

# Circuit breaker events
curl http://localhost:8080/actuator/circuitbreakerevents
```

### Grafana Dashboard

A dedicated dashboard is available at **Grafana → Dashboards → Resilience4j** showing:
- Circuit breaker states (open/closed/half-open)
- Failure rates
- Retry counts
- Timeout events

Access: http://localhost:3000 → Dashboards → Devices - Resilience4j

## Why Reactor Transformers?

### Not AOP (@CircuitBreaker, @Retry)

AOP annotations don't work well with reactive streams (`Mono`/`Flux`):
- Circuit breaker triggers on **completion**, not **subscription**
- Errors in reactive streams may not trigger circuit breakers correctly
- Thread-local context lost in async operations

### Reactor Transformers

Transformers integrate directly with reactive streams:
- Wraps the entire reactive chain
- Circuit breaker triggers on **subscription**
- Retry handles reactive errors correctly
- Time limiter kills slow reactive operations

### Example

```java
// ❌ Bad: AOP annotation doesn't work with reactive
@CircuitBreaker(name = "devices")
@Retry(name = "devices")
public Mono<Device> findById(Long id) { ... }

// ✅ Good: Reactor transformers
public Mono<Device> findById(Long id) {
    return applyResilience(
        databaseClient
            .select()
            .from("devices")
            .matching(where("id").is(id))
            .fetch()
            .one()
    );
}
```

## Testing

### Unit Tests

Resilience is tested indirectly through integration tests:
- Real PostgreSQL database via Testcontainers
- Circuit breaker triggers on database failures
- Retry attempts visible in logs
- Timeout protection tested with slow queries

### Performance Tests

Use K6 profiles to test resilience under load:

```bash
# Run load test with resilience enabled
make k6-load

# Verify circuit breaker triggers under high load
# Check metrics at http://localhost:8080/actuator/metrics
```

### Manual Testing

1. **Test Circuit Breaker**: Simulate database failures
2. **Test Retry**: Enable slow query logs and verify retries
3. **Test Timeout**: Make database very slow and verify 5s timeout

## Configuration Files

- `src/main/resources/application.properties`: Production settings
- `src/main/resources/application-k6.properties`: Relaxed settings for performance testing

**Note:** Resilience4j registries (`CircuitBreakerRegistry`, `RetryRegistry`, `TimeLimiterRegistry`) are auto-configured by Spring Boot when `resilience4j-spring-boot3` dependency is present. No manual configuration class needed - registries are injected as beans automatically.

## Best Practices

1. **Always use transformers**, not AOP annotations for reactive code
2. **Layer timeouts**: Connection < Statement < Operation
3. **Monitor metrics**: Watch circuit breaker states in Grafana
4. **Test resilience**: Use K6 to verify under load
5. **Fail fast**: Timeouts prevent cascading failures

## Troubleshooting

### Circuit Breaker Always Open

- Check failure rate threshold (default: 50%)
- Verify minimum number of calls (default: 5)
- Review recent failures in logs

### Retries Not Working

- Verify retry configuration is loaded
- Check if errors are retryable (network errors are, business errors aren't)
- Review retry metrics in Grafana

### Timeouts Too Aggressive

- Increase `timeoutDuration` for slow operations
- Check if database is healthy
- Review query performance

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Boot Resilience4j](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.resilience4j)
- [Project Reactor Transformers](https://projectreactor.io/docs/core/release/reference/index.html#transformers)

