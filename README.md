# IoT Devices Management System

![CI](https://github.com/rodolfodpk/devices/workflows/CI/badge.svg)
[![codecov](https://codecov.io/github/rodolfodpk/devices/graph/badge.svg?token=9XOKJB6039)](https://codecov.io/github/rodolfodpk/devices)
![License](https://img.shields.io/badge/license-Copyright-orange)

A comprehensive IoT device management system built with Spring Boot 3.5.6 and reactive programming (Spring WebFlux).

## Technology Stack

- **Framework**: Spring Boot 3.5.6 (Java 25)
- **Reactive Stack**: Spring WebFlux, Project Reactor, R2DBC
- **Database**: PostgreSQL 17.2 (Reactive)
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Timeout)
- **Observability**: Prometheus, Grafana, Spring Boot Actuator
- **Testing**: JUnit 5, Testcontainers, K6

## Quick Start

### Prerequisites

- Java 25, Maven 3.9+, Docker & Docker Compose

### Running the Application

```bash
# Start application with PostgreSQL
make start

# Start application with observability stack (Prometheus + Grafana)
make start-obs

# Run all tests
make test

# Stop application
make stop
```

### Available URLs

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus UI**: http://localhost:9090

### Common Commands

```bash
make help       # Show all available commands
make start      # Start PostgreSQL and application
make start-obs  # Start with observability stack
make test       # Run all tests (48 tests, ~11 seconds)
make stop       # Stop all services
make logs       # Show application logs
```

## Documentation

- **[Testing Guide](docs/TESTING.md)** - Comprehensive test strategy and coverage
- **[K6 Performance Testing](docs/K6_PERFORMANCE.md)** - Load testing with K6
- **[Observability](docs/OBSERVABILITY.md)** - Monitoring and Grafana dashboards

## Key Features

- ✅ **Reactive Programming**: Full non-blocking architecture
- ✅ **Resilience Patterns**: Circuit breakers, retries, timeouts
- ✅ **Observability**: Structured logging, metrics, dashboards
- ✅ **API Documentation**: Auto-generated Swagger UI
- ✅ **Testing**: 48 tests covering domain, repository, service, controller, and E2E flows
- ✅ **Performance Testing**: K6 load, stress, and spike tests

## Architecture

Package-per-layer architecture with comprehensive integration tests:

- **Controller**: REST API endpoints
- **Service**: Business logic and domain validation
- **Repository**: Database access with Resilience4j transformers
- **Domain**: Entities and business rules

## Resilience Patterns

Layered timeout strategy ensures fast failure:

```
Resilience4j TimeLimiter (5s) → R2DBC Statement (4s) → R2DBC Connection (3s)
```

All database operations protected with:
- Circuit Breaker (opens after 50% failure rate)
- Retry (3 attempts with 1s delay)
- Timeout (5s overall operation timeout)

Note: Rate Limiter is configured but not applied to database operations.

## License

Copyright © 2025
