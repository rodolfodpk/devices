# IoT Devices Management System

![CI](https://github.com/rodolfodpk/devices/workflows/CI/badge.svg)
![Codecov](https://codecov.io/gh/rodolfodpk/devices/branch/main/graph/badge.svg)
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

The application implements comprehensive resilience patterns:

- **Circuit Breaker**: Protects against cascading failures
- **Retry**: Automatic retry for transient failures
- **Timeout**: Prevents hanging requests
- **Bulkhead**: Isolates thread pools
- **Rate Limiter**: Prevents overloading

Configure in `application.properties`:

```properties
# Circuit Breaker
resilience4j.circuitbreaker.instances.devices-config.slidingWindowSize=10

# Retry
resilience4j.retry.instances.devices-config.maxAttempts=3

# Timeout
resilience4j.timelimiter.instances.devices-config.timeoutDuration=5s

# Rate Limiter
resilience4j.ratelimiter.instances.devices-config.limitForPeriod=100
```

## License

Copyright © 2025

