# Architecture

Package-per-layer architecture for the IoT Devices Management System.

## Architecture Overview

The application follows a **package-per-layer** architecture pattern, where each package represents a specific layer of the application.

```
src/main/java/com/rdpk/
├── Application.java              # Main application class
├── config/                        # Configuration classes
│   ├── DatabaseConfig.java
│   └── ResilienceConfig.java
└── device/                        # Device feature (package-per-layer)
    ├── controller/                # REST API layer
    ├── service/                   # Business logic layer
    ├── repository/                # Data access layer
    ├── domain/                    # Domain models
    ├── dto/                       # Data Transfer Objects
    └── exception/                 # Custom exceptions
```

## Layer Structure

### 1. Controller Layer
**Package:** `com.rdpk.device.controller`

**Responsibilities:**
- Handle HTTP requests
- Validate request data
- Delegate to service layer
- Return HTTP responses

**Rules:**
- `@RestController` annotation required
- Must end with `Controller` suffix
- Can only depend on service layer (no direct repository access)

**Example:**
```java
@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService deviceService;
    // ...
}
```

### 2. Service Layer
**Package:** `com.rdpk.device.service`

**Responsibilities:**
- Implement business logic
- Enforce domain validation rules
- Coordinate between controller and repository

**Rules:**
- `@Service` annotation required
- Must end with `Service` suffix
- Can depend on repository and domain layers only

**Example:**
```java
@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    // ...
}
```

### 3. Repository Layer
**Package:** `com.rdpk.device.repository`

**Responsibilities:**
- Database access
- Query execution
- Data persistence

**Rules:**
- `@Repository` annotation required
- Must end with `Repository` suffix
- Can only depend on domain layer
- Apply Resilience4j transformers to database operations

**Example:**
```java
@Repository
public class DeviceRepositoryImpl implements DeviceRepository {
    // Applies Circuit Breaker, Retry, Timeout
}
```

### 4. Domain Layer
**Package:** `com.rdpk.device.domain`

**Responsibilities:**
- Domain entities
- Business rules
- State management

**Rules:**
- No Spring annotations
- No dependencies on other layers
- Pure domain logic
- Immutable records

**Example:**
```java
public record Device(
    Long id,
    String name,
    String brand,
    DeviceState state,
    LocalDateTime createdAt
) {
    public boolean isInUse() { ... }
    public boolean isDeletable() { ... }
}
```

### 5. DTO Layer
**Package:** `com.rdpk.device.dto`

**Responsibilities:**
- Request/Response objects
- Data validation

**Rules:**
- No Spring annotations
- Bean Validation annotations allowed
- Pure data structures

### 6. Exception Layer
**Package:** `com.rdpk.device.exception`

**Responsibilities:**
- Custom exceptions
- Error definitions

**Rules:**
- No Spring annotations
- Standalone classes

## Dependency Rules

### Allowed Dependencies

```
Controller → Service
         → Domain
         → DTO
         → Exception

Service → Repository
       → Domain
       → Exception

Repository → Domain

Domain → (no dependencies)
```

### Prohibited Dependencies

- ❌ Controller → Repository (use Service instead)
- ❌ Service → Controller (circular dependency)
- ❌ Repository → Service, Controller
- ❌ Domain → Spring annotations, other layers
- ❌ DTO → Spring annotations
- ❌ Exception → Spring annotations

## Architecture Enforcement

Architecture rules are enforced using **ArchUnit** tests.

### Running Architecture Tests

```bash
# Run all tests including architecture tests
mvn test

# Run only architecture tests
mvn test -Dtest=DeviceArchitectureTest
```

### Architecture Rules Verified

1. **Controllers**
   - Must be annotated with `@RestController`
   - Must end with `Controller` suffix
   - Cannot depend on repository layer

2. **Services**
   - Must be annotated with `@Service`
   - Must end with `Service` suffix
   - Cannot depend on controller layer

3. **Repositories**
   - Must be annotated with `@Repository`
   - Must end with `Repository` suffix
   - Cannot depend on service or controller layers

4. **Domain**
   - No Spring annotations allowed
   - Pure domain logic
   - Immutable records

5. **DTOs**
   - No Spring annotations
   - Data validation only

## Resilience Patterns

Database operations in the repository layer are protected with Resilience4j using Reactor transformers.

See **[Resilience Documentation](RESILIENCE.md)** for details on:
- Circuit Breaker configuration
- Retry strategy
- Layered timeout strategy
- Reactor transformer usage

## Best Practices

1. **Immutability**: Use records for domain entities and DTOs
2. **Reactive**: Use `Mono` and `Flux` for async operations
3. **Validation**: Enforce validation rules in service layer
4. **Error Handling**: Use domain-specific exceptions
5. **Resilience**: Apply circuit breakers to all database operations
6. **Testing**: Use Testcontainers for integration tests (no mocks)

## Testing Strategy

### Architecture Tests
- Enforce package structure
- Verify dependency rules
- Check naming conventions

### Integration Tests
- Real PostgreSQL via Testcontainers
- Test each layer in isolation
- Verify business rules

### E2E Tests
- Test complete workflows
- Verify API contracts
- Test domain validations

