# IoT Devices Management System

![CI](https://github.com/rodolfodpk/devices/workflows/CI/badge.svg)
[![codecov](https://codecov.io/github/rodolfodpk/devices/graph/badge.svg?token=9XOKJB6039)](https://codecov.io/github/rodolfodpk/devices)
![License](https://img.shields.io/badge/license-MIT-green)

IoT device management system built with Spring Boot 3.5.7 and reactive programming (Spring WebFlux).

## Technology Stack

- **Framework**: Spring Boot 3.5.7 (Java 25)
- **Reactive Stack**: Spring WebFlux, Project Reactor, R2DBC
- **Database**: PostgreSQL 17.2 (accessed via R2DBC)
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Timeout)
- **Observability**: Prometheus, Grafana, Spring Boot Actuator
- **Testing**: JUnit 5, Testcontainers, K6

## Quick Start

### Prerequisites

- **For Development**: Java 25, Maven 3.9+, Docker & Docker Compose
- **For Testing Only**: Java 25, Maven 3.9+ (Testcontainers provides PostgreSQL automatically)

### Running the Application

```bash
# Start application with PostgreSQL
make start

# Start application with observability stack (Prometheus + Grafana)
make start-obs

# Run all tests (uses Testcontainers, no Docker Compose needed)
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

#### Application Management
```bash
make start       # Start PostgreSQL and application
make start-obs   # Start with observability stack (Prometheus + Grafana)
make start-k6    # Start application with K6 testing profile
make stop        # Stop all services
make restart     # Restart application and PostgreSQL
make logs        # Show application logs
make health      # Check application health status
```

#### Testing
```bash
make test        # Run all tests (68 tests, ~11 seconds, uses Testcontainers)
```

#### Observability
```bash
make grafana     # Open Grafana dashboard
make prometheus  # Open Prometheus UI
make metrics     # View application metrics
```

#### Development
```bash
make clean       # Clean build artifacts and containers
make build       # Build the application
make setup       # Initial setup (clean + build + start)
```

#### K6 Performance Testing
```bash
make k6-smoke    # Smoke test (5 VUs, 1 minute)
make k6-load     # Load test (50 VUs, 9 minutes)
make k6-stress   # Stress test (finds breaking point)
make k6-spike    # Spike test (sudden traffic surge)
```

Run `make help` to see all available commands.

## Documentation

- **[Architecture](docs/ARCHITECTURE.md)** - Package structure and dependency rules
- **[Resilience](docs/RESILIENCE.md)** - Circuit Breaker, Retry, and Timeout patterns
- **[Testing Guide](docs/TESTING.md)** - Test strategy and coverage
- **[K6 Performance Testing](docs/K6_PERFORMANCE.md)** - Load testing with K6
- **[Observability](docs/OBSERVABILITY.md)** - Monitoring and Grafana dashboards

## Key Features

- ✅ **Reactive Programming**: Non-blocking architecture
- ✅ **Resilience Patterns**: Circuit breakers, retries, timeouts
- ✅ **Observability**: Structured logging, metrics, dashboards
- ✅ **API Documentation**: Auto-generated Swagger UI
- ✅ **Testing**: 68 tests (Testcontainers, no Docker Compose needed)
- ✅ **Performance Testing**: K6 load, stress, and spike tests

## Domain Model

**Device States:**
- `AVAILABLE` - Device is available for use
- `IN_USE` - Device is currently being used
- `INACTIVE` - Device is no longer active in the system

**Domain Rules:**
- Creation time cannot be updated
- Name and brand cannot be updated if device is IN_USE
- Devices in IN_USE or INACTIVE state cannot be deleted
- INACTIVE devices can be updated (to allow reactivation)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
