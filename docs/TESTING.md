# Testing Guide

Comprehensive testing strategy for the IoT Devices Management System using JUnit 5 and Testcontainers.

## Test Strategy

The project uses **package-per-layer** architecture with comprehensive integration and E2E tests:

- ✅ **Domain Tests** (7 tests) - Domain model validation
- ✅ **Repository Tests** (9 tests) - Database operations with real PostgreSQL
- ✅ **Service Tests** (15 tests) - Business logic with real repository
- ✅ **Controller Tests** (13 tests) - HTTP endpoints with WebTestClient
- ✅ **E2E Tests** (4 tests) - Complete application flows
- ✅ **Observability Tests** (2 tests) - Actuator endpoints
- **Total: 50 tests, 0 failures**

All integration tests use **Testcontainers** for real PostgreSQL database - **NO MOCKS**.

## Running Tests

### Run All Tests

```bash
# Run all tests (~11 seconds)
make test

# Or directly with Maven
mvn test
```

### Run Specific Test Classes

```bash
# Domain tests
mvn test -Dtest=DeviceTest

# Repository tests
mvn test -Dtest=DeviceRepositoryIntegrationTest

# Service tests
mvn test -Dtest=DeviceServiceIntegrationTest

# Controller tests
mvn test -Dtest=DeviceControllerIntegrationTest

# E2E tests
mvn test -Dtest=DeviceE2ETest

# Observability tests
mvn test -Dtest=ObservabilityIntegrationTest
```

### Run with Coverage Report

```bash
mvn verify
```

Coverage reports are generated in `target/site/jacoco/index.html`.

## Test Performance

- **Execution Time:** ~11 seconds for 50 tests
- **Container Reuse:** Shared PostgreSQL container across all tests
- **Isolation:** Database cleanup via `TRUNCATE ... RESTART IDENTITY CASCADE` in `@BeforeEach`
- **No @DirtiesContext:** Using `@TestInstance(PER_CLASS)` for performance
- **Testcontainers:** PostgreSQL container with `withReuse(true)` for fast tests

## Test Structure

### Package-Per-Layer Architecture

```
src/test/java/com/rdpk/
├── device/
│   ├── domain/              # Domain unit tests
│   │   └── DeviceTest.java
│   ├── integration/
│   │   ├── repository/      # Repository integration tests
│   │   │   └── DeviceRepositoryIntegrationTest.java
│   │   ├── service/          # Service integration tests
│   │   │   └── DeviceServiceIntegrationTest.java
│   │   ├── controller/       # Controller integration tests
│   │   │   └── DeviceControllerIntegrationTest.java
│   │   └── observability/   # Actuator tests
│   │       └── ObservabilityIntegrationTest.java
│   ├── e2e/                 # End-to-end tests
│   │   └── DeviceE2ETest.java
│   └── fixture/             # Test data builders
│       └── DeviceFixture.java
├── AbstractIntegrationTest.java    # Base test class
└── config/
    └── SharedPostgresContainer.java # Singleton container
```

### Base Test Class

All integration tests extend `AbstractIntegrationTest`:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {
    
    @Autowired
    protected WebTestClient webTestClient;
    
    @Autowired
    protected DatabaseClient databaseClient;
    
    @BeforeEach
    void setUp() {
        // Clean database before each test
        databaseClient.sql("TRUNCATE TABLE devices RESTART IDENTITY CASCADE")
            .fetch().rowsUpdated().block();
    }
}
```

### Singleton Container

`SharedPostgresContainer` ensures a single PostgreSQL container is shared across all tests:

```java
public class SharedPostgresContainer {
    private static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:17.2")
        .withReuse(true);
    
    public static PostgreSQLContainer<?> getInstance() {
        return INSTANCE;
    }
}
```

## Test Coverage

### Domain Validation Rules

Tests verify critical business rules:

✅ **Creation time immutability**
- `createdAt` is set once during creation
- Cannot be modified via update operations

✅ **In-use device restrictions**
- `name` and `brand` cannot be updated when device is `IN_USE`
- Device must be `AVAILABLE` to modify these properties

✅ **Deletion protection**
- Devices with state `IN_USE` cannot be deleted
- Device must be `AVAILABLE` to delete

### Test Examples

**Domain Test:**
```java
@Test
void shouldNotAllowNameUpdateWhenInUse() {
    Device device = deviceRepository.save(
        fixture.createAvailableDevice("Sensor")
    ).block();
    
    Device inUseDevice = device.withState(DeviceState.IN_USE);
    deviceRepository.save(inUseDevice).block();
    
    assertThatThrownBy(() -> 
        deviceService.updateDevice(inUseDevice.id(), 
            UpdateDeviceRequest.builder()
                .name("NewName")
                .build())
    ).isInstanceOf(DeviceUpdateException.class);
}
```

**E2E Test:**
```java
@Test
void shouldEnforceBusinessRulesForInUseDevices() {
    // Create device
    CreateDeviceResponse created = createDevice("Camera", "Ring");
    
    // Change to IN_USE
    updateDeviceState(created.id(), DeviceState.IN_USE);
    
    // Try to update name (should fail)
    webTestClient.patch()
        .uri("/api/v1/devices/{id}", created.id())
        .bodyValue(UpdateDeviceRequest.builder()
            .name("New Camera")
            .build())
        .exchange()
        .expectStatus().isBadRequest();
}
```

## Test Categories

### 1. Domain Tests (`DeviceTest.java`)

Unit tests for domain model validation:
- Device creation
- State transitions
- Property updates
- Business rule validation

**Count:** 7 tests

### 2. Repository Tests (`DeviceRepositoryIntegrationTest.java`)

Integration tests with real PostgreSQL:
- Save operation (insert)
- Save operation (update)
- Find by ID
- Find all
- Find by brand
- Find by state
- Delete operation
- Edge cases

**Count:** 9 tests

### 3. Service Tests (`DeviceServiceIntegrationTest.java`)

Business logic tests with real repository:
- Create device
- Get all devices
- Get device by ID
- Get devices by brand
- Get devices by state
- Update device name
- Update device brand
- Update device state
- Delete device
- Create time immutability
- In-use device restrictions
- Deletion protection

**Count:** 15 tests

### 4. Controller Tests (`DeviceControllerIntegrationTest.java`)

HTTP endpoint tests with WebTestClient:
- POST /api/v1/devices
- GET /api/v1/devices
- GET /api/v1/devices?brand=X
- GET /api/v1/devices?state=Y
- GET /api/v1/devices/{id}
- PATCH /api/v1/devices/{id}
- DELETE /api/v1/devices/{id}
- Error handling (404, 400)

**Count:** 13 tests

### 5. E2E Tests (`DeviceE2ETest.java`)

End-to-end tests covering complete flows:
- Create → Get → Update → Delete
- Business rules enforcement
- Concurrent operations
- Error scenarios

**Count:** 4 tests

### 6. Observability Tests (`ObservabilityIntegrationTest.java`)

Actuator endpoint tests:
- Health check endpoint
- Metrics endpoint
- Circuit breaker metrics

**Count:** 2 tests

## Code Coverage

Current test coverage focuses on:
- ✅ All domain models
- ✅ All repository methods
- ✅ All service methods
- ✅ All controller endpoints
- ✅ All business validation rules
- ✅ All exception scenarios

## Best Practices

1. **Real Database:** Never use mocks for database operations
2. **Testcontainers:** Always use Testcontainers for integration tests
3. **Shared Container:** Use singleton pattern for PostgreSQL container
4. **Database Cleanup:** Truncate tables in `@BeforeEach`, not `@AfterEach`
5. **No @DirtiesContext:** Use `@TestInstance(PER_CLASS)` for performance
6. **Test Fixtures:** Use builders for test data creation
7. **Meaningful Names:** Use descriptive test method names
8. **Independent Tests:** Each test should be independent

## Continuous Integration

Tests run automatically on every push via GitHub Actions:

```yaml
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '25'
      - run: mvn test
      - run: mvn verify
      - uses: codecov/codecov-action@v5
```

## Troubleshooting

**Issue:** Tests are slow
- **Solution:** Ensure container reuse is enabled in `SharedPostgresContainer`

**Issue:** Database state leaks between tests
- **Solution:** Check that `TRUNCATE ... RESTART IDENTITY CASCADE` runs in `@BeforeEach`

**Issue:** Container start fails
- **Solution:** Ensure Docker is running and has sufficient resources allocated

**Issue:** Port conflicts
- **Solution:** Verify no other PostgreSQL instance is running on port 5432

## Additional Resources

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [WebTestClient Guide](https://docs.spring.io/spring-framework/reference/testing/webtestclient.html)
- [DatabaseClient Guide](https://docs.spring.io/spring-framework/reference/data-access/r2dbc.html)

