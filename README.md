# IoT Devices Management System

![CI](https://github.com/rodolfodpk/devices/workflows/CI/badge.svg)
[![codecov](https://codecov.io/github/rodolfodpk/devices/graph/badge.svg?token=9XOKJB6039)](https://codecov.io/github/rodolfodpk/devices)
![License](https://img.shields.io/badge/license-Copyright-orange)

A comprehensive IoT device management system built with Spring Boot 3.5.6 and reactive programming (Spring WebFlux).

## Technology Stack

- **Framework**: Spring Boot 3.5.6 (Java 25)
- **Reactive Stack**: Spring WebFlux, Project Reactor, R2DBC
- **Database**: PostgreSQL 17.2 (Reactive)
- **Build Tool**: Maven 3.9+
- **Testing**: JUnit 5, Testcontainers
- **Documentation**: SpringDoc OpenAPI 3 (Swagger)

## Features

- 🚀 **Reactive Programming**: Full non-blocking architecture
- 🛡️ **Resilience Patterns**: Circuit breakers, retries, timeouts, bulkheads, rate limiters
- 📊 **Observability**: Spring Boot Actuator integration
- 📝 **API Documentation**: Auto-generated Swagger UI
- 🔄 **Database Migrations**: Flyway for schema management
- ✅ **Testing**: Comprehensive test coverage with Testcontainers

## Quick Start

### Prerequisites

- Java 25
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 17.2+ (or use Docker Compose)

### Running the Application

```bash
# Start the application with PostgreSQL
make start

# Or build and start manually
mvn clean install
docker-compose up -d
mvn spring-boot:run
```

### Available Endpoints

Once running, access:

- **Application**: http://localhost:8080
- **API Documentation (Swagger)**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus

### Observability Stack (with `make start-obs`)

- **Grafana**: http://localhost:3000 (admin/admin) - Visual dashboards and metrics
- **Prometheus**: http://localhost:9090 - Metrics collection and querying

## Project Structure

```
src/
├── main/
│   ├── java/com/rdpk/
│   │   ├── Application.java          # Main application class
│   │   ├── config/                    # Configuration classes
│   │   └── device/                    # Device feature (Package-per-layer)
│   │       ├── controller/            # REST API controllers
│   │       ├── service/               # Business logic services
│   │       ├── repository/            # Database access layer
│   │       ├── domain/                # Domain models (entities, enums)
│   │       ├── dto/                   # Data Transfer Objects
│   │       └── exception/             # Custom exceptions
│   └── resources/
│       ├── application.properties     # Main configuration
│       └── db/migration/              # Flyway migrations
└── test/
    ├── java/com/rdpk/
    │   ├── device/
    │   │   ├── domain/                # Domain unit tests
    │   │   ├── integration/
    │   │   │   ├── repository/        # Repository integration tests
    │   │   │   ├── service/           # Service integration tests
    │   │   │   └── controller/        # Controller integration tests
    │   │   ├── e2e/                   # End-to-end tests
    │   │   └── fixture/               # Test data builders
    │   ├── AbstractIntegrationTest.java # Base test class with Testcontainers
    │   └── config/
    │       └── SharedPostgresContainer.java # Singleton container
    └── resources/
        └── application-test.properties
```

## Development

### Commands

```bash
make help       # Show all available commands
make start      # Start PostgreSQL and application
make stop       # Stop PostgreSQL and application
make test       # Run all tests
make build      # Build the application
make clean      # Clean build artifacts and containers
```

## Testing

### Test Strategy

The project uses **package-per-layer** architecture with comprehensive integration and E2E tests:

- ✅ **Domain Tests** (7 tests) - Domain model validation
- ✅ **Repository Tests** (9 tests) - Database operations with real PostgreSQL
- ✅ **Service Tests** (15 tests) - Business logic with real repository  
- ✅ **Controller Tests** (13 tests) - HTTP endpoints with WebTestClient
- ✅ **E2E Tests** (4 tests) - Complete application flows
- **Total: 48 tests, 0 failures**

All integration tests use **Testcontainers** for real PostgreSQL database - **NO MOCKS**.

### Running Tests

```bash
# Run all tests (~11 seconds)
mvn test

# Run with coverage report
mvn verify

# Run specific test class
mvn test -Dtest=DeviceServiceIntegrationTest
```

### Test Performance

- **Execution Time**: ~11 seconds for 48 tests
- **Container Reuse**: Shared PostgreSQL container across all tests
- **Isolation**: Database cleanup via TRUNCATE in @BeforeEach
- **No @DirtiesContext**: Using @TestInstance(PER_CLASS) for performance

### Test Coverage

Tests cover domain validation rules:
- ✅ Creation time immutability
- ✅ In-use device restrictions (name/brand updates blocked)
- ✅ Deletion protection for in-use devices

## Database

The application uses PostgreSQL with R2DBC for reactive database access and Flyway for schema migrations.

### Connection Details

- **Host**: localhost
- **Port**: 5432
- **Database**: devices
- **Username**: devices
- **Password**: devices

## Resilience Patterns

The application implements comprehensive resilience patterns using **Resilience4j with Reactor transformers** (not annotations):

- **Circuit Breaker**: Protects against cascading database failures (opens after 50% failure rate)
- **Retry**: Automatic retry for transient failures (3 attempts with 1s delay)
- **Timeout**: Prevents hanging requests (5s overall operation timeout)
- **Rate Limiter**: Prevents overloading (100 req/s limit)

### Layered Timeout Strategy

The application uses a **3-layer timeout strategy** to ensure fast failure at each level:

```
Layer 3: Resilience4j TimeLimiter (5s) - Overall operation timeout
    ↓
Layer 2: R2DBC Statement Timeout (4s) - Query execution timeout
    ↓
Layer 1: R2DBC Connection Timeout (3s) - Connection establishment timeout
```

Each layer fails fast before the next layer triggers, preventing cascade issues.

### Implementation

Resilience4j transformers are applied to all database operations in `DeviceRepositoryImpl` using `.transformDeferred()`:

```java
return databaseClient.sql("SELECT...")
    .map(...)
    .one()
    .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("devices")))
    .transformDeferred(RetryOperator.of(retryRegistry.retry("devices")))
    .transformDeferred(TimeLimiterOperator.of(timeLimiterRegistry.timeLimiter("devices")));
```

### Configuration

Configure in `application.properties`:

```properties
# R2DBC Connection Factory Timeout (layered timeout strategy)
spring.r2dbc.properties.connectTimeout=3s        # Connection establishment
spring.r2dbc.properties.statementTimeout=4s      # Query execution

# Resilience4j Configuration
resilience4j.circuitbreaker.instances.devices.slidingWindowSize=10
resilience4j.circuitbreaker.instances.devices.failureRateThreshold=50
resilience4j.circuitbreaker.instances.devices.waitDurationInOpenState=60000

resilience4j.retry.instances.devices.maxAttempts=3
resilience4j.retry.instances.devices.waitDuration=1000

resilience4j.timelimiter.instances.devices.timeoutDuration=5s

resilience4j.ratelimiter.instances.devices.limitForPeriod=100
resilience4j.ratelimiter.instances.devices.limitRefreshPeriod=PT1S
```

### K6 Testing Profile

For performance testing, use the K6 profile with relaxed rate limits:

```bash
make start-k6  # Uses application-k6.properties (no rate limits)
```

The K6 profile removes rate limits to allow unlimited throughput for load testing.

## Observability

The application includes comprehensive observability with structured logging, metrics collection, and visualization.

### Structured Logging

JSON-formatted logs for production environments:
- **Development**: Pretty-printed console output
- **Production**: JSON structured logs with correlation IDs
- **Format**: Includes timestamp, level, logger, message, and MDC context

Example JSON log:
```json
{
  "@timestamp": "2025-10-28T18:00:00.000Z",
  "level": "INFO",
  "logger": "com.rdpk.device.service.DeviceService",
  "message": "Device created successfully",
  "application": "devices"
}
```

### Metrics and Monitoring

Start the observability stack:
```bash
make start-obs
```

Access monitoring tools:
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Application Metrics**: http://localhost:8080/actuator/prometheus

Useful commands:
```bash
make start-obs    # Start application with Prometheus & Grafana
make grafana      # Open Grafana dashboard
make prometheus   # Open Prometheus UI
make metrics      # View raw metrics
make stop-obs     # Stop observability stack
```

### Metrics Collected

The application exports the following metrics:
- **HTTP**: Request count, latency, status codes
- **JVM**: Memory usage, thread count, GC pauses
- **Resilience4j**: Circuit breaker state, retry attempts, timeout occurrences
- **Database**: Connection pool metrics, query duration

### Logging Configuration

Logging is configured via `logback-spring.xml`:
- **Production**: JSON format for log aggregation tools
- **Development**: Human-readable format for local development
- **Log Levels**: Configurable per package and environment

## License

Copyright © 2025

