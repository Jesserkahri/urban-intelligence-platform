# Urban Intelligence Platform - AI/ML Extensibility Guide

## Overview

The Urban Intelligence Platform backend is architected with AI/ML integration as a first-class concern. This guide explains the expansion path from rule-based analytics to intelligent ML-powered predictions and recommendations.

## Current State (Rule-Based Analytics)

### Present Capabilities

- **Hotspot Detection**: Geographic clustering of incidents
- **Risk Scoring**: Multi-factor district risk calculation
- **Trend Analysis**: Time-series pattern detection
- **Anomaly Detection**: Threshold-based spike detection

### Example: Rule-Based Hotspot Detection

```java
public Map<String, Integer> detectIncidentHotspots() {
    LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
    List<Incident> recentIncidents = incidentRepository.findByCreatedAtAfter(twentyFourHoursAgo);

    Map<String, Integer> hotspots = new HashMap<>();
    for (Incident incident : recentIncidents) {
        String clusterKey = generateClusterKey(incident.getLatitude(), incident.getLongitude());
        hotspots.put(clusterKey, hotspots.getOrDefault(clusterKey, 0) + 1);
    }

    return hotspots;
}
```

## Phase 1: ML-Ready Data Infrastructure

### Objective

Establish foundation for machine learning by organizing historical data

### Implementation

#### 1. Add Time-Series Specific Queries

```java
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // For training datasets
    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :start AND :end " +
           "ORDER BY i.createdAt ASC")
    List<Incident> findIncidentsBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    // For feature engineering
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.district = :district " +
           "AND DATE(i.createdAt) = :date AND i.severity = :severity")
    Long countIncidentsForFeature(
        @Param("district") District district,
        @Param("date") LocalDateTime date,
        @Param("severity") Incident.SeverityLevel severity);
}
```

#### 2. Add Windowing/Bucketing Utilities

```java
@Service
public class DataBucketingService {

    public Map<LocalDateTime, Long> bucketIncidentsByHour(
            LocalDateTime start, LocalDateTime end, Long districtId) {
        // Returns hourly incident counts for time-series analysis
    }

    public Map<String, List<Incident>> bucketIncidentsByType(
            LocalDateTime start, LocalDateTime end) {
        // Groups incidents by type for classification training
    }
}
```

#### 3. Add Feature Engineering Service

```java
@Service
public class FeatureEngineeringService {

    public IncidentFeatureSet extractFeatures(Incident incident) {
        return IncidentFeatureSet.builder()
            .latitude(incident.getLatitude())
            .longitude(incident.getLongitude())
            .dayOfWeek(incident.getCreatedAt().getDayOfWeek().getValue())
            .hourOfDay(incident.getCreatedAt().getHour())
            .severityScore(mapSeverityToScore(incident.getSeverity()))
            .districtRiskScore(incident.getDistrict().getOperationalRiskScore())
            .build();
    }
}
```

## Phase 2: Lightweight ML Model Integration

### Objective

Integrate first machine learning model for incident prediction

### Implementation Path

#### 1. Create ML Model Service Interface

```java
@Service
public class IncidentPredictionService {

    private final MLModelClient mlModelClient;
    private final FeatureEngineeringService featureEngineeringService;

    /**
     * Predict severity of future incident at given location
     */
    public PredictionResult predictIncidentSeverity(
            Double latitude, Double longitude, String type) {

        IncidentFeatureSet features = featureEngineeringService
            .extractLocationFeatures(latitude, longitude, type);

        // Call ML model (hosted locally or external service)
        ModelPrediction prediction = mlModelClient.predict(features);

        return new PredictionResult(
            prediction.getPredictedSeverity(),
            prediction.getConfidenceScore(),
            prediction.getRecommendedAction()
        );
    }

    /**
     * Forecast incident frequency for district
     */
    public IncidentForecast forecastIncidentFrequency(Long districtId, int daysAhead) {
        // Time-series forecasting model
    }
}
```

#### 2. Update Recommendation Service with ML

```java
@Service
@Transactional
public class RecommendationService {

    private final IncidentPredictionService predictionService;
    private final AIRecommendationEngine aiEngine;

    /**
     * Generate recommendations with AI assistance
     */
    public RecommendationResponse generateAIRecommendation(Long districtId) {
        District district = districtRepository.findById(districtId).orElseThrow();

        // Get AI-powered recommendations
        List<String> aiSuggestions = aiEngine.generateRecommendations(district);

        // Convert to Recommendation entities
        Recommendation rec = Recommendation.builder()
            .type("AI_GENERATED")
            .priority(Recommendation.Priority.HIGH)
            .message(aiSuggestions.get(0))
            .district(district)
            .build();

        return mapToResponse(recommendationRepository.save(rec));
    }
}
```

#### 3. Add ML Endpoint to Analytics Controller

```java
@RestController
@RequestMapping("/api/analytics/ml")
public class MLAnalyticsController {

    private final IncidentPredictionService predictionService;

    @PostMapping("/predict/severity")
    public ResponseEntity<PredictionResult> predictSeverity(
            @RequestBody SeverityPredictionRequest request) {
        return ResponseEntity.ok(
            predictionService.predictIncidentSeverity(
                request.getLatitude(),
                request.getLongitude(),
                request.getType()
            )
        );
    }

    @GetMapping("/forecast/district/{id}")
    public ResponseEntity<IncidentForecast> forecastDistrict(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") int daysAhead) {
        return ResponseEntity.ok(
            predictionService.forecastIncidentFrequency(id, daysAhead)
        );
    }
}
```

## Phase 3: Advanced ML Capabilities

### Objective

Deploy sophisticated predictive and optimization models

### Use Case Examples

#### 1. Predictive Maintenance

```java
@Service
public class MaintenancePredictionService {

    private final MLModelClient mlModelClient;

    /**
     * Predict infrastructure maintenance needs
     */
    public MaintenanceSchedule predictMaintenance(Long districtId) {
        District district = districtRepository.findById(districtId).orElseThrow();

        List<MaintenanceFeature> features = extractMaintenanceFeatures(district);
        List<MaintenancePrediction> predictions = mlModelClient
            .predictMaintenanceNeeds(features);

        return MaintenanceSchedule.builder()
            .districtId(districtId)
            .predictions(predictions)
            .priority(calculatePriority(predictions))
            .build();
    }
}
```

#### 2. Resource Optimization

```java
@Service
public class ResourceOptimizationService {

    /**
     * Optimize patrol and resource allocation
     */
    public ResourceDeploymentPlan optimizeResourceAllocation() {
        List<District> districts = districtRepository.findAll();

        // Use optimization model (linear programming, genetic algorithms, etc.)
        ResourceDeploymentPlan plan = optimizationEngine.compute(
            districts,
            availableResources,
            constraints
        );

        return plan;
    }
}
```

#### 3. Demand Forecasting

```java
@Service
public class DemandForecastingService {

    /**
     * Forecast demand for services/infrastructure
     */
    public ServiceDemandForecast forecastDemand(String serviceType, LocalDateTime period) {
        List<AnalyticsEvent> historicalData = analyticsEventRepository
            .findByTimestampBetween(
                period.minusMonths(12),
                period
            );

        ServiceDemandForecast forecast = forecastingModel.predict(
            historicalData,
            serviceType,
            period
        );

        return forecast;
    }
}
```

## Implementation Approaches

### Option 1: External ML Service (Recommended for Production)

#### Architecture

```
Urban Intelligence Platform
    ↓ (HTTP/gRPC)
ML Service Container (Python/FastAPI)
    ├─ TensorFlow/PyTorch models
    ├─ Feature scaling pipeline
    └─ Model serving framework
    ↓
Database (shared)
```

#### Benefits

- Language flexibility (Python/R for ML)
- Independent scaling
- Model versioning/experimentation
- Team separation

#### Implementation

```java
@Component
public class ExternalMLClient {

    private final RestTemplate restTemplate;

    public PredictionResult predict(IncidentFeatureSet features) {
        HttpEntity<IncidentFeatureSet> request = new HttpEntity<>(features);
        return restTemplate.postForObject(
            "http://ml-service:5000/api/predict",
            request,
            PredictionResult.class
        );
    }
}
```

### Option 2: Embedded ML Models (Quick Start)

#### Libraries

- **ONNX Runtime Java**: Load trained models
- **TensorFlow Java**: Direct model inference
- **XGBoost4J**: Gradient boosting

#### Implementation

```java
@Service
public class EmbeddedMLService {

    private final OnnxEnvironment env;
    private OrtSession session;

    @PostConstruct
    public void loadModel() {
        session = env.createSession("models/incident_severity.onnx");
    }

    public SeverityPrediction predict(float[] features) {
        OrtSession.Result output = session.run(
            Collections.singletonMap("features", features)
        );
        return new SeverityPrediction(output);
    }
}
```

### Option 3: Cloud ML Platforms

#### AWS

- **SageMaker**: Managed ML service
- **Lambda**: Serverless model execution

#### Azure

- **Azure ML**: Integrated ML platform
- **Cognitive Services**: Pre-built models

#### Implementation

```java
@Service
public class AzureMLClient {

    private final AzureSageMakerClient mlClient;

    public Prediction predict(String endpointName, InputData data) {
        return mlClient.invokeEndpoint(endpointName, data);
    }
}
```

## Data Pipeline for ML

### Data Flow

```
Raw Data Collection
    ├─ Incidents API → AnalyticsEvent
    ├─ External sensors → AnalyticsEvent
    └─ Events logs → AnalyticsEvent
        ↓
Data Aggregation Service
    ├─ Bucketing by time period
    ├─ Feature engineering
    └─ Data normalization
        ↓
ML Training Dataset (External)
    ├─ Historical incidents (12 months)
    ├─ Feature vectors
    └─ Target labels
        ↓
Model Training (External ML Pipeline)
    ├─ Data preprocessing
    ├─ Feature selection
    ├─ Model training
    └─ Cross-validation
        ↓
Model Serving (Java App or External Service)
    ├─ Load trained model
    ├─ Run predictions
    └─ Return results via API
```

## Monitoring ML Performance

### Add ML Metrics

```java
@Service
public class MLMetricsService {

    private final MeterRegistry meterRegistry;

    public void recordPrediction(PredictionResult result) {
        meterRegistry.counter(
            "ml.predictions.total",
            "model", result.getModelName(),
            "confidence", result.getConfidenceScore() > 0.8 ? "high" : "low"
        ).increment();

        meterRegistry.timer(
            "ml.prediction.latency",
            "model", result.getModelName()
        ).record(result.getLatencyMs(), TimeUnit.MILLISECONDS);
    }
}
```

### Dashboard Integration

- Track model accuracy over time
- Monitor prediction latency
- Alert on data drift

## Security for ML

### Model Security

```java
@Service
public class ModelAccessService {

    @PreAuthorize("hasRole('ANALYTICS')")
    public PredictionResult predict(InputData data) {
        // Only analytics services can call models
    }
}
```

### Data Privacy

- Anonymize PII before model input
- Encrypt model artifacts
- Audit model access

## Testing ML Components

```java
@SpringBootTest
public class IncidentPredictionServiceTest {

    @Test
    public void testPredictionWithMockModel() {
        // Mock ML service
        when(mlModelClient.predict(any()))
            .thenReturn(new PredictionResult("HIGH", 0.92));

        PredictionResult result = predictionService.predict(features);

        assertEquals("HIGH", result.getSeverity());
        assertEquals(0.92, result.getConfidence());
    }
}
```

## Migration Path Summary

| Phase   | Timeline    | Capabilities                                   | Infrastructure           |
| ------- | ----------- | ---------------------------------------------- | ------------------------ |
| Current | Now         | Rule-based analytics                           | Java application         |
| Phase 1 | Months 1-2  | ML-ready data pipeline                         | PostgreSQL + Features    |
| Phase 2 | Months 2-3  | First ML model (severity prediction)           | External ML service      |
| Phase 3 | Months 3-6  | Advanced predictions, optimization             | ML pipeline + monitoring |
| Phase 4 | Months 6-12 | Real-time ML inference, reinforcement learning | Kubernetes, GPU support  |

## Example: Complete ML Recommendation Flow

```java
@Service
@Transactional
public class IntelligentRecommendationService {

    private final IncidentPredictionService predictions;
    private final ResourceOptimizationService optimization;
    private final DemandForecastingService forecasting;

    @PostMapping("/api/recommendations/intelligent/{districtId}")
    public RecommendationResponse generateIntelligentRecommendation(
            @PathVariable Long districtId) {

        District district = districtRepository.findById(districtId).orElseThrow();

        // 1. Predict future incidents
        IncidentForecast forecast = predictions.forecastIncidentFrequency(districtId, 7);

        // 2. Forecast demand
        ServiceDemandForecast demand = forecasting.forecastDemand("patrol", now());

        // 3. Optimize resources
        ResourcePlan plan = optimization.optimizeResourceAllocation(
            forecast, demand, district);

        // 4. Generate recommendation
        String message = String.format(
            "Based on ML analysis: Expected %d incidents next week. " +
            "Recommended deployment: %s. Forecasted demand: %s",
            forecast.getExpectedIncidents(),
            plan.getRecommendedDeployment(),
            demand.getPeakHours()
        );

        Recommendation rec = Recommendation.builder()
            .type("ML_OPTIMIZED")
            .priority(Recommendation.Priority.HIGH)
            .message(message)
            .district(district)
            .build();

        return mapToResponse(recommendationRepository.save(rec));
    }
}
```

---

**ML Roadmap Status**: Foundation Ready  
**Next Steps**: Phase 1 implementation (data pipeline optimization)  
**Review Date**: August 2024
