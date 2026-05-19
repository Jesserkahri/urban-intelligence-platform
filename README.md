# Urban Intelligence Platform - Backend Foundation

A production-grade intelligent civic analytics platform for urban operations, sustainability metrics, and public infrastructure insights.

## 🏗️ Architecture Overview

### Clean Layered Architecture

```
API Layer (Controllers)
    ↓
Business Logic Layer (Services)
    ↓
Data Access Layer (Repositories)
    ↓
Domain Model (Entities)
    ↓
Database (PostgreSQL)
```

### Key Components

1. **Domain Layer** (`domain/`)
   - Entity models with JPA annotations
   - Repositories with custom query methods
   - Domain-driven design principles

2. **Service Layer** (`service/`)
   - Business logic encapsulation
   - DTO conversion and mapping
   - Transaction management

3. **API Layer** (`api/`)
   - REST controllers with request validation
   - Exception handling with custom error responses
   - Request/Response DTOs

4. **Analytics Layer** (`analytics/`)
   - Hotspot detection
   - District risk scoring
   - Trend aggregation
   - Operational insights generation

5. **Configuration** (`config/`)
   - JWT authentication framework
   - Security configuration
   - Application properties

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 14+
- Git

### Setup Steps

#### 1. Database Setup

```sql
CREATE DATABASE urban_intelligence_db;
CREATE USER urban_user WITH PASSWORD 'secure_password_here';
GRANT ALL PRIVILEGES ON DATABASE urban_intelligence_db TO urban_user;
```

#### 2. Clone and Build

```bash
cd "Urban flagship"
mvn clean install
```

#### 3. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/urban_intelligence_db
spring.datasource.username=urban_user
spring.datasource.password=secure_password_here
app.jwt.secret=your-secret-key-min-256-bits
```

#### 4. Run Application

```bash
mvn spring-boot:run
```

Server starts at: `http://localhost:8080`

## 📋 API Endpoints Overview

### Districts Management

```
POST   /api/districts                  - Create district
GET    /api/districts                  - List all districts (paginated)
GET    /api/districts/{id}             - Get district by ID
GET    /api/districts/{id}/metrics     - Get district health metrics
GET    /api/districts/risk-analysis/highest - Districts ranked by risk
GET    /api/districts/sustainability/below?threshold=70 - Low sustainability districts
PUT    /api/districts/{id}             - Update district
DELETE /api/districts/{id}             - Delete district
```

### Incidents Management

```
POST   /api/incidents                  - Create incident
GET    /api/incidents                  - List all incidents (paginated)
GET    /api/incidents/{id}             - Get incident by ID
GET    /api/incidents/district/{districtId} - Incidents by district
GET    /api/incidents/status/{status}  - Incidents by status
GET    /api/incidents/district/{id}/active - Active incidents for district
GET    /api/incidents/recent           - Last 7 days incidents
PUT    /api/incidents/{id}             - Update incident
DELETE /api/incidents/{id}             - Delete incident
```

### Recommendations Management

```
POST   /api/recommendations            - Create recommendation
GET    /api/recommendations            - List all recommendations (paginated)
GET    /api/recommendations/{id}       - Get recommendation by ID
GET    /api/recommendations/district/{districtId} - Recommendations by district
GET    /api/recommendations/priority/{priority} - By priority level
GET    /api/recommendations/district/{id}/urgent - Urgent recommendations
PUT    /api/recommendations/{id}       - Update recommendation
DELETE /api/recommendations/{id}       - Delete recommendation
```

### Analytics Events

```
POST   /api/analytics/events           - Record analytics event
GET    /api/analytics/events           - List events (paginated)
GET    /api/analytics/events/{id}      - Get event by ID
GET    /api/analytics/events/category/{category} - Events by category
GET    /api/analytics/events/source/{source} - Events by source
GET    /api/analytics/events/recent/high-scoring - Recent high-scoring events
GET    /api/analytics/aggregates/{category} - Category statistics
DELETE /api/analytics/events/{id}      - Delete event
```

## 🧪 Manual API Testing

### Using cURL

#### 1. Create a District

```bash
curl -X POST http://localhost:8080/api/districts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Downtown District",
    "population": 50000,
    "sustainabilityScore": 75.5,
    "operationalRiskScore": 35.2
  }'
```

#### 2. Create an Incident

```bash
curl -X POST http://localhost:8080/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "type": "Traffic Congestion",
    "description": "Major traffic congestion on Main Street",
    "severity": "HIGH",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "districtId": 1
  }'
```

#### 3. Get District Metrics

```bash
curl -X GET http://localhost:8080/api/districts/1/metrics
```

#### 4. Record Analytics Event

```bash
curl -X POST http://localhost:8080/api/analytics/events \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Air Quality",
    "score": 68.5,
    "source": "Environmental Sensor",
    "metadata": "{\"location\": \"Downtown\"}"
  }'
```

#### 5. Get Category Aggregates

```bash
curl -X GET "http://localhost:8080/api/analytics/aggregates/Air%20Quality"
```

### Using Postman

1. Import the collection from `docs/postman-collection.json` (create this)
2. Set environment variables for base URL
3. Execute requests with pre-configured templates

### Using Thunder Client (VS Code Extension)

1. Import collection from `docs/thunder-collection.json` (create this)
2. Execute API calls directly from VS Code

## 📊 Data Models

### District

```json
{
  "id": 1,
  "name": "Downtown District",
  "population": 50000,
  "sustainability_score": 75.5,
  "operational_risk_score": 35.2,
  "incident_count": 5,
  "recommendation_count": 3
}
```

### Incident

```json
{
  "id": 1,
  "type": "Traffic Congestion",
  "description": "Major traffic congestion on Main Street",
  "severity": "HIGH",
  "latitude": 40.7128,
  "longitude": -74.006,
  "district_id": 1,
  "status": "IN_PROGRESS",
  "created_at": "2024-05-19T10:30:00",
  "updated_at": "2024-05-19T11:00:00"
}
```

### Recommendation

```json
{
  "id": 1,
  "type": "Traffic Management",
  "priority": "HIGH",
  "message": "Implement traffic signal optimization in Downtown District",
  "district_id": 1,
  "district_name": "Downtown District",
  "created_at": "2024-05-19T09:00:00",
  "updated_at": "2024-05-19T09:00:00"
}
```

### AnalyticsEvent

```json
{
  "id": 1,
  "category": "Air Quality",
  "score": 68.5,
  "source": "Environmental Sensor",
  "timestamp": "2024-05-19T10:15:00",
  "metadata": "{\"location\": \"Downtown\"}"
}
```

## 🔍 Analytics Services

### Hotspot Detection

- Identifies geographic clusters of incidents
- Uses spatial proximity analysis
- Returns high-density incident areas

### District Risk Scoring

Comprehensive risk calculation:

- Operational Risk (40% weight)
- Recent Incident Frequency (35% weight)
- Sustainability Metrics (15% weight)
- Urgent Recommendations Ratio (10% weight)

Risk Levels: LOW, MEDIUM, HIGH, CRITICAL

### Trend Aggregation

- Analyzes incident type patterns
- Detects activity anomalies
- Computes category distributions
- Calculates trend velocity

### Operational Insights

- Dashboard-level summaries
- System health status
- Critical alerts
- Operational recommendations
- Trend analysis

## 🔐 Security Foundation

### JWT Authentication Framework

- Token generation via `JwtTokenProvider`
- Configurable expiration (default: 24 hours)
- Support for analytics service tokens
- Foundation for OAuth2 integration

### Future Security Enhancements

- API key authentication for service-to-service
- OAuth2 authorization code flow
- Role-based access control (RBAC)
- API rate limiting and throttling

## 🧬 Extensibility for AI/ML

The architecture is designed for seamless AI/ML expansion:

### 1. Analytics Events Pipeline

- AnalyticsEvent entity captures raw data
- Time-series storage ready for ML training
- Metadata field for model-specific features

### 2. Prediction Framework Foundation

```java
// Future ML module
@Service
public class PredictionService {
    // Incident prediction
    // Demand forecasting
    // Anomaly detection
    // Resource optimization
}
```

### 3. AI Service Integration Points

- Analytics controller can route to ML models
- Recommendation service can be enhanced with AI suggestions
- Risk scoring can incorporate ML predictions

### 4. Data Pipeline Ready

- Event aggregation supports ML feature engineering
- Time-bucketing queries for training datasets
- Category/type analysis for classification tasks

## 📈 Future Smart-City Analytics

### Potential ML Use Cases

1. **Incident Prediction**: Forecast incidents by type, location, time
2. **Resource Optimization**: Optimal patrol/maintenance deployment
3. **Demand Forecasting**: Infrastructure capacity planning
4. **Anomaly Detection**: Unusual event patterns
5. **Recommendation Engine**: AI-generated operational suggestions

### Integration Pathways

- Kafka for real-time ML model serving (optional)
- TensorFlow/PyTorch model integration
- External ML platforms (Azure ML, AWS SageMaker)
- Time-series databases for historical analysis

## 🛠️ Development Workflow

### Add New Feature

1. Create entity in `domain/entity/`
2. Create repository in `domain/repository/`
3. Create service in `service/`
4. Create DTOs in `dto/`
5. Create controller in `api/controller/`
6. Add exception handling if needed

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=IncidentServiceTest

# Coverage report
mvn jacoco:report
```

## 📝 Project Structure

```
Urban flagship/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/urban/intelligence/platform/
│   │   │       ├── UrbanIntelligencePlatformApplication.java
│   │   │       ├── api/
│   │   │       │   ├── controller/
│   │   │       │   │   ├── IncidentController.java
│   │   │       │   │   ├── DistrictController.java
│   │   │       │   │   ├── RecommendationController.java
│   │   │       │   │   └── AnalyticsController.java
│   │   │       │   └── exception/
│   │   │       │       ├── GlobalExceptionHandler.java
│   │   │       │       ├── ResourceNotFoundException.java
│   │   │       │       └── ApiError.java
│   │   │       ├── analytics/
│   │   │       │   ├── HotspotDetectionService.java
│   │   │       │   ├── DistrictRiskScoringService.java
│   │   │       │   ├── TrendAggregationService.java
│   │   │       │   └── OperationalInsightService.java
│   │   │       ├── config/
│   │   │       │   ├── JwtProperties.java
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── domain/
│   │   │       │   ├── entity/
│   │   │       │   │   ├── Incident.java
│   │   │       │   │   ├── District.java
│   │   │       │   │   ├── Recommendation.java
│   │   │       │   │   └── AnalyticsEvent.java
│   │   │       │   └── repository/
│   │   │       │       ├── IncidentRepository.java
│   │   │       │       ├── DistrictRepository.java
│   │   │       │       ├── RecommendationRepository.java
│   │   │       │       └── AnalyticsEventRepository.java
│   │   │       ├── dto/
│   │   │       │   ├── IncidentDTO.java
│   │   │       │   ├── DistrictDTO.java
│   │   │       │   ├── RecommendationDTO.java
│   │   │       │   └── AnalyticsDTO.java
│   │   │       └── service/
│   │   │           ├── IncidentService.java
│   │   │           ├── DistrictService.java
│   │   │           ├── RecommendationService.java
│   │   │           └── AnalyticsEventService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/
└── README.md
```

## 🚀 Deployment

### Docker Support (Recommended)

```bash
docker build -t urban-intelligence-platform:1.0.0 .
docker run -p 8080:8080 --env-file .env urban-intelligence-platform:1.0.0
```

### Cloud Deployment

- **AWS**: ECS/Fargate, RDS for PostgreSQL
- **Azure**: App Service, Azure Database for PostgreSQL
- **GCP**: Cloud Run, Cloud SQL

## 📞 Support & Contribution

For issues or feature requests, create an issue in the repository.

## 📄 License

[Add appropriate license information]

---

**Version**: 1.0.0  
**Last Updated**: May 19, 2024  
**Status**: Production Foundation
