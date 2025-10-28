# IoT Devices Management System

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
│   │   └── features/                  # Domain-driven features
│   └── resources/
│       ├── application.properties     # Main configuration
│       └── db/migration/              # Flyway migrations
└── test/
    └── java/com/rdpk/                 # Test classes
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

### Unit & Integration Tests

```bash
mvn test
```

### E2E Tests

```bash
mvn verify
```

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

