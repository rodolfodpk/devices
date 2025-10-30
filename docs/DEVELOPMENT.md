# Development Guide

Complete development guide for the IoT Devices Management System including all make commands and workflows.

## Quick Start

```bash
# Run tests (requires Docker to be running)
make test

# Start application
make start

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

## Make Commands

### Application Management

#### Starting the Application

```bash
make start          # Start PostgreSQL and the application
make start-obs      # Start with observability stack (Prometheus + Grafana)
make start-k6       # Start application with K6 testing profile
make start-k6-obs   # Start with observability stack + K6 profile (for monitoring K6 tests)
make start-resilience # Start with resilience-test profile
```

**Note:** `make start-k6-obs` automatically runs `docker-compose down -v` to clean all volumes before starting, ensuring a fresh database state for K6 performance testing.

#### Managing the Application

```bash
make stop           # Stop all services
make restart        # Restart application and PostgreSQL
make logs           # Show application logs
make health         # Check application health status
```

### Testing

#### Unit and Integration Tests

```bash
make test           # Run all tests (68 tests, ~11 seconds)
```

**Note:** Tests use Testcontainers, so Docker must be running. PostgreSQL is automatically provided by Testcontainers - no Docker Compose needed.

#### K6 Performance Testing

```bash
# Individual tests
make k6-smoke       # Smoke test (5 VUs, 1 minute)
make k6-load        # Load test (50 VUs, 9 minutes)
make k6-stress      # Stress test (finds breaking point)
make k6-spike       # Spike test (sudden traffic surge)
make k6-concurrent  # Concurrent test (race conditions)
make k6-mixed       # Mixed workload test (realistic simulation)

# Automated workflows
make k6-test              # Run all K6 tests (smoke, load, stress, spike, concurrent, mixed)
make k6-test-automated    # Automated workflow (reset + smoke + load + cleanup)
make k6-test-quick        # Quick test (reset + smoke + cleanup)

# Individual test runner
make k6-test-individual   # Run each test individually with cleanup

# Tests with fresh database
make k6-test-individual-fresh # Individual tests with fresh database
make k6-smoke-fresh       # Smoke test with fresh database
make k6-load-fresh        # Load test with fresh database

# Production simulation
make k6-production-simulation # Production-like testing sequence
```

### Observability

```bash
# Start observability stack
make start-obs      # Start application with Prometheus and Grafana (prod profile)
make start-k6-obs   # Start with observability stack + K6 profile (for monitoring K6 tests)
                    # Automatically cleans volumes (docker-compose down -v) for fresh state

# Access dashboards
make grafana        # Open Grafana dashboard (http://localhost:3000)
make prometheus     # Open Prometheus UI (http://localhost:9090)
make metrics        # View application metrics
```

### Development Workflow

```bash
# Setup
make setup          # Initial setup (clean + build + start)

# Building
make build          # Build the application
make clean          # Clean build artifacts and containers

# Development cycle
make dev-cycle      # Complete dev cycle (clean + build + start)
```

### Database Management

```bash
# Database operations
make db-reset       # Reset database (stop, remove volumes, restart)
make db-clean-quick # Quick database cleanup (TRUNCATE tables)
```

### Help

```bash
make help          # Show all available commands
```

## Development Workflow

### 1. Initial Setup

```bash
# Clone the repository
git clone https://github.com/rodolfodpk/devices.git
cd devices

# Run tests to verify everything works
make test

# Start the application
make start
```

### 2. Running Tests

#### Unit and Integration Tests

All tests use Testcontainers for PostgreSQL. No Docker Compose needed:

```bash
# Run all tests
make test

# Output: 68 tests, ~11 seconds
```

Test Categories:
- Domain Model Tests (12)
- Architecture Tests (13)
- Repository Integration Tests (9)
- Service Integration Tests (15)
- Controller Integration Tests (13)
- E2E Tests (4)
- Observability Integration Tests (2)

#### K6 Performance Tests

K6 tests require the application to be running:

```bash
# Start application with K6 profile
make start-k6

# In another terminal, run K6 tests
make k6-smoke    # Quick validation
make k6-load     # Normal load
make k6-stress   # Find breaking point
```

Or use automated workflow:

```bash
make k6-test-automated  # Does everything: reset + start + test + cleanup
```

### 3. Development Cycle

```bash
# Clean, build, and start
make dev-cycle

# Make changes to code...

# Run tests
make test

# Check logs
make logs

# Stop when done
make stop
```

### 4. Observability

Start with observability stack:

```bash
make start-obs

# Access Grafana
make grafana
# Login: admin / admin

# Access Prometheus
make prometheus

# View metrics
make metrics
```

## K6 Performance Testing

### Prerequisites

1. Application must be running (use `make start-k6`)
2. K6 installed: `brew install k6` or `curl https://github.com/grafana/k6/releases/latest/download/k6 -L -o /usr/local/bin/k6`

### Running Tests

#### Quick Validation (Smoke Test)
```bash
make k6-smoke
# 5 VUs, 1 minute duration
# Validates basic functionality
```

#### Normal Load
```bash
make k6-load
# 50 VUs, 9 minutes duration
# Simulates expected production load
```

#### Find Breaking Point (Stress Test)
```bash
make k6-stress
# Ramps from 10 to 300 VUs over 20 minutes
# Identifies system limits
```

#### Sudden Traffic Spike
```bash
make k6-spike
# Spikes from 10→200→10 VUs over 5 minutes
# Tests circuit breakers
```

#### Automated Full Workflow
```bash
make k6-test-automated
# Automated workflow:
# 1. Stop existing processes
# 2. Reset database
# 3. Start application
# 4. Run smoke test
# 5. Run load test
# 6. Cleanup
```

### K6 Test Results

K6 outputs results to terminal including:
- Request duration (min/avg/max)
- Requests per second
- HTTP status codes
- Data transferred
- Iteration duration
- Error rate

Example output:
```
http_req_duration........: avg=45ms  min=12ms  med=38ms  max=234ms
http_reqs................: 15000 500/s
http_req_failed..........: 0%   0/15000
```

## Environment Variables

### Application Profiles

- `default` - Production profile (standard settings)
- `k6` - K6 testing profile (relaxed resilience settings)
- `resilience-test` - Resilience testing profile (aggressive limits)

### Spring Profiles

Set via environment variable or command line:

```bash
# Using environment variable
export SPRING_PROFILES_ACTIVE=k6
make start

# Using command line
mvn spring-boot:run -Dspring-boot.run.profiles=k6
```

## Docker Commands

### PostgreSQL Container

```bash
# Start PostgreSQL only
docker-compose up -d postgres

# Stop PostgreSQL
docker-compose stop postgres

# View PostgreSQL logs
docker-compose logs postgres

# Access PostgreSQL
docker exec -it devices-postgres psql -U devices -d devices
```

### Full Observability Stack

```bash
# Start all services (PostgreSQL, Prometheus, Grafana)
docker-compose up -d

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# View all logs
docker-compose logs -f
```

## Troubleshooting

### Tests Failing

**Problem:** Tests fail with PostgreSQL connection errors

**Solution:**
```bash
# Ensure Docker is running
docker ps

# Restart Testcontainers
docker system prune -f

# Run tests again
make test
```

### Application Won't Start

**Problem:** Port 8080 already in use

**Solution:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or stop all services
make stop
```

### K6 Tests Not Running

**Problem:** K6 tests fail with connection refused

**Solution:**
```bash
# Ensure application is running
make start-k6

# Wait for application to be ready
sleep 30

# Run K6 test
make k6-smoke
```

## CI/CD

### GitHub Actions

Tests run automatically on push to main branch:

```bash
# See .github/workflows/ci.yml for details
```

Manual trigger:
```bash
# Push to trigger CI
git push origin main
```

## Available URLs

When application is running:

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **All Metrics**: http://localhost:8080/actuator/metrics
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus UI**: http://localhost:9090

## Maven Commands

If you prefer Maven directly:

```bash
# Run tests
mvn test

# Build
mvn clean package

# Run application
mvn spring-boot:run

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=k6
```

## Performance Optimization

### For Fast Test Execution

```bash
# Use existing PostgreSQL container (faster)
docker-compose up -d postgres

# Reuse test containers
export TESTCONTAINERS_REUSE_ENABLE=true

# Run tests
make test
```

### For Faster Builds

```bash
# Skip tests during build
mvn clean package -DskipTests

# Use offline mode (if dependencies cached)
mvn clean package -o
```

## Next Steps

- **[Architecture Documentation](ARCHITECTURE.md)** - Package structure and dependency rules
- **[Testing Guide](TESTING.md)** - Test strategy and coverage details
- **[Resilience Documentation](RESILIENCE.md)** - Circuit Breaker, Retry, and Timeout patterns
- **[K6 Performance Testing](K6_PERFORMANCE.md)** - Detailed K6 testing guide
- **[Observability Documentation](OBSERVABILITY.md)** - Monitoring setup and dashboards

