# Urban Intelligence Platform - Architecture Documentation

## Overview

The Urban Intelligence Platform is built on **Clean Layered Architecture** principles, emphasizing separation of concerns, maintainability, and extensibility for AI/ML integration.

## Architectural Layers

### 1. API Layer (`api/`)

**Responsibility**: HTTP request handling and response formatting

```
GET /api/incidents
    ↓
IncidentController
    ├─ Validates incoming request
    ├─ Calls IncidentService
    └─ Returns IncidentResponse DTO
```

**Key Components**:

- **Controllers**: REST endpoints with `@RestController`
- **DTOs**: Request/Response data transfer objects
- **Exception Handler**: Centralized error handling

**Why**: Insulates business logic from HTTP concerns

### 2. Service Layer (`service/`)

**Responsibility**: Business logic and domain operations

```
IncidentService
    ├─ Orchestrates repositories
    ├─ Performs validations
    ├─ Manages transactions
    └─ Converts entities to DTOs
```

**Key Components**:

- **IncidentService**: Incident CRUD and operations
- **DistrictService**: District management with risk analysis
- **RecommendationService**: Recommendation operations
- **AnalyticsEventService**: Analytics data management

**Why**: Encapsulates domain logic, enables testing, supports multiple clients

### 3. Domain Layer (`domain/`)

**Responsibility**: Core business entities and data access

```
Incident (Entity)
    ├─ Domain rules (business validation)
    ├─ Entity lifecycle (audit timestamps)
    └─ Relationships (district, incidents)
```

**Components**:

- **Entities**: JPA-annotated domain models
  - `Incident`: Urban incidents with spatial/temporal data
  - `District`: Administrative regions with metrics
  - `Recommendation`: AI/analytics-generated suggestions
  - `AnalyticsEvent`: Raw analytics data points

- **Repositories**: Spring Data JPA interfaces
  - Custom query methods for specific operations
  - Pagination support
  - Bulk operations

**Why**: Maintains single source of truth, enables database flexibility

### 4. Analytics Layer (`analytics/`)

**Responsibility**: Intelligent analysis and insights generation

```
HotspotDetectionService
    ├─ Spatial clustering
    ├─ Geographic analysis
    └─ High-risk area identification

DistrictRiskScoringService
    ├─ Multi-factor risk calculation
    ├─ Component weighting
    └─ Risk categorization

TrendAggregationService
    ├─ Temporal pattern analysis
    ├─ Anomaly detection
    └─ Velocity calculation

OperationalInsightService
    ├─ Synthesizes all data
    ├─ Generates recommendations
    └─ Dashboard aggregation
```

**Why**: Separates analytical logic from CRUD operations, prepares for ML integration

### 5. Configuration Layer (`config/`)

**Responsibility**: Cross-cutting configuration concerns

```
JwtTokenProvider
    ├─ Token generation
    ├─ Claim management
    └─ Expiration handling

SecurityConfig
    ├─ Password encoding
    ├─ Security beans
    └─ Auth framework setup
```

**Why**: Centralizes configuration, enables environment-specific settings

## Data Flow Example: Create Incident

```
1. HTTP Request
   POST /api/incidents
   {
     "type": "Traffic Congestion",
     "severity": "HIGH",
     "districtId": 1,
     ...
   }
        ↓
2. Controller (IncidentController)
   - Validates @Valid annotation
   - Calls service method
        ↓
3. Service (IncidentService)
   - Validates business rules
   - Fetches District from repository
   - Creates Incident entity
   - Saves via repository
   - Converts to DTO
        ↓
4. Repository (IncidentRepository)
   - Executes SQL INSERT
   - Returns saved entity with ID
        ↓
5. Response
   {
     "id": 42,
     "type": "Traffic Congestion",
     "status": "REPORTED",
     "created_at": "2024-05-19T10:30:00",
     ...
   }
```

## Design Patterns Used

### 1. Repository Pattern

Abstracts data access logic behind interfaces

```java
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByDistrict_Id(Long districtId);
    // Custom queries hide SQL complexity
}
```

**Benefit**: Easy to switch database implementations, mockable for testing

### 2. Service Locator / Dependency Injection

Spring manages all bean creation and injection

```java
@Service
@RequiredArgsConstructor // Generates constructor with final fields
public class IncidentService {
    private final IncidentRepository incidentRepository;
    // Spring injects automatically
}
```

**Benefit**: Loose coupling, testable, configurable

### 3. DTO Pattern

Data transfer objects decouple internal representation from APIs

```java
// Internal entity
@Entity
public class Incident { ... }

// API response DTO
@Getter @Setter
public class IncidentResponse { ... }
```

**Benefit**: API contracts remain stable, internal changes isolated

### 4. Builder Pattern

Fluent object construction

```java
Incident incident = Incident.builder()
    .type("Traffic")
    .severity(SeverityLevel.HIGH)
    .build();
```

**Benefit**: Readable, handles optional fields, immutability support

### 5. Strategy Pattern (Analytics)

Different analytical strategies encapsulated

```java
// HotspotDetectionService uses different strategies
Map<String, Integer> hotspots = detectIncidentHotspots();
// vs
List<Map<String, Object>> risks = calculateDistrictRiskScore();
```

**Benefit**: Easy to add new analysis types without affecting existing code

## Transaction Management

### Declarative Transactions

```java
@Service
@Transactional
public class IncidentService {
    // All public methods have transactional context

    public IncidentResponse createIncident(...) {
        // Automatic rollback on exception
        // Automatic commit on success
    }
}
```

### Read-Only Optimization

```java
@Transactional(readOnly = true)
public Page<IncidentResponse> getAllIncidents(...) {
    // Database optimization - no flush needed
}
```

**Benefit**: ACID compliance, performance optimization

## Error Handling Strategy

### Exception Hierarchy

```
Exception
├─ ResourceNotFoundException
│  └─ Thrown when entity not found
├─ IllegalArgumentException
│  └─ Thrown for validation failures
└─ Generic Exception
   └─ Unknown errors
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(...) {
        // Converts exceptions to JSON error responses
    }
}
```

**Benefit**: Consistent error responses, reduced controller clutter

## Pagination & Performance

### Pagination Support

```java
@GetMapping
public ResponseEntity<Page<IncidentResponse>> getAllIncidents(
    @PageableDefault(size = 20, sort = "createdAt", direction = DESC)
    Pageable pageable
) {
    // Spring handles offset/limit automatically
}
```

### Database Indexing

```sql
CREATE INDEX idx_incident_district ON incidents(district_id);
CREATE INDEX idx_incident_created_at ON incidents(created_at);
```

**Benefit**: O(1) lookups, efficient pagination

## Validation Strategy

### Bean Validation

```java
@NotBlank(message = "Type is required")
@NotNull(message = "Severity is required")
public class IncidentCreateRequest {
    // Validated by @Valid annotation
}
```

### Business Validation

```java
if (districtRepository.findByName(name).isPresent()) {
    throw new IllegalArgumentException("District already exists");
}
```

**Benefit**: Multi-layer validation, clear error messages

## Logging Strategy

### Structured Logging

```java
log.info("Creating new incident of type: {}", request.getType());
log.debug("Fetching incidents for district: {}", districtId);
log.warn("Unusual activity pattern detected");
log.error("Failed to process request", exception);
```

### Log Levels

- `DEBUG`: Development/troubleshooting
- `INFO`: Important business events
- `WARN`: Potentially harmful situations
- `ERROR`: Error events with error handling

**Benefit**: Operational visibility, debugging support

## Extensibility Points

### Adding New Domain Entity

1. Create entity in `domain/entity/`
2. Add JPA annotations and relationships
3. Create repository in `domain/repository/`
4. Create DTOs in `dto/`
5. Create service in `service/`
6. Create controller in `api/controller/`

### Integrating ML Models

1. Create `service/MLPredictionService`
2. Add predictions to analytics layer
3. Update recommendation generation
4. Expose through new API endpoint

### Adding Analytics Algorithm

1. Extend `analytics/` package
2. Implement algorithm as service
3. Call from `OperationalInsightService`
4. Expose through analytics controller

## Security Architecture

### Current Foundation

- JWT token generation framework
- Password encoding support
- Exception handling for auth failures

### Future Enhancements

1. **Authentication**: JWT validation filter
2. **Authorization**: Role-based access control (RBAC)
3. **API Keys**: Service-to-service authentication
4. **Rate Limiting**: Request throttling
5. **HTTPS**: Transport security

## Deployment Architecture

### Container Deployment

```
Docker Image (Urban Intelligence Platform)
└─ Contains compiled JAR
   ├─ Spring Boot embedded Tomcat
   ├─ Application code
   └─ Configuration

PostgreSQL Container
├─ Database server
└─ Persistent volume
```

### Environment Separation

- **Development**: `application-dev.properties` (DDL auto-create)
- **Production**: `application-prod.properties` (DDL validate)

## Monitoring & Observability

### Actuator Endpoints

```
/actuator/health       - Application health status
/actuator/metrics      - Performance metrics
/actuator/info         - Application information
```

### Custom Logging

- Audit logs for critical operations
- Performance logs for slow queries
- Error logs with stack traces

## Performance Considerations

### Query Optimization

- N+1 query prevention with JPA joins
- Pagination for large result sets
- Custom queries for complex operations

### Caching Strategy (Future)

- Repository-level caching
- Analytics result caching
- Distributed cache (Redis)

### Connection Pooling

- HikariCP for connection management
- Configurable pool size (default: 10)
- Connection timeout management

## Backward Compatibility

### DTO Versioning

- API responses include only non-null fields
- New fields added without breaking clients
- Deprecated endpoints maintained

### Migration Strategy

- Database schema versions
- Liquibase/Flyway ready (not yet implemented)
- Zero-downtime deployment support

---

**Architecture Review Date**: May 19, 2024  
**Status**: Production Foundation  
**Next Review**: Architecture review before major feature additions
