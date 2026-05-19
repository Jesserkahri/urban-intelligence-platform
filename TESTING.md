# Manual API Testing Guide

## Prerequisites

- Application running on `http://localhost:8080`
- PostgreSQL database initialized
- Sample data loaded

## API Testing Tools

### Option 1: cURL (Command Line)

```bash
# Works on all platforms, no installation needed
curl -X GET http://localhost:8080/api/districts
```

### Option 2: Postman

```bash
# Download: https://www.postman.com/
# Import provided collection for ready-to-use requests
```

### Option 3: Thunder Client (VS Code)

```bash
# Install extension from VS Code Marketplace
# Import provided collection
```

### Option 4: HTTPie (Recommended for Humans)

```bash
# Install: brew install httpie (macOS) or apt-get install httpie (Linux)
# Usage: http GET http://localhost:8080/api/districts
```

## Testing Workflow

### 1. Health Check

**Verify application is running:**

```bash
curl -i http://localhost:8080/api/districts
# Should return 200 OK (empty list initially)
```

### 2. Create District

**Endpoint**: `POST /api/districts`

**Request**:

```bash
curl -X POST http://localhost:8080/api/districts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Downtown District",
    "population": 85000,
    "sustainabilityScore": 72.5,
    "operationalRiskScore": 45.3
  }'
```

**Expected Response** (201 Created):

```json
{
  "id": 1,
  "name": "Downtown District",
  "population": 85000,
  "sustainability_score": 72.5,
  "operational_risk_score": 45.3,
  "incident_count": 0,
  "recommendation_count": 0
}
```

**Save the returned ID for subsequent requests** (example: `1`)

### 3. Get All Districts (Paginated)

**Endpoint**: `GET /api/districts?page=0&size=10`

```bash
curl "http://localhost:8080/api/districts?page=0&size=10"
```

**Expected Response**:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Downtown District",
      ...
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalElements": 1
  }
}
```

### 4. Get Single District

**Endpoint**: `GET /api/districts/{id}`

```bash
curl http://localhost:8080/api/districts/1
```

### 5. Create Incident

**Endpoint**: `POST /api/incidents`

**Request**:

```bash
curl -X POST http://localhost:8080/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "type": "Traffic Congestion",
    "description": "Heavy traffic on Main Street due to construction",
    "severity": "HIGH",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "districtId": 1
  }'
```

**Expected Response** (201 Created):

```json
{
  "id": 1,
  "type": "Traffic Congestion",
  "description": "Heavy traffic on Main Street due to construction",
  "severity": "HIGH",
  "latitude": 40.7128,
  "longitude": -74.006,
  "district_id": 1,
  "status": "REPORTED",
  "created_at": "2024-05-19T10:30:00",
  "updated_at": "2024-05-19T10:30:00"
}
```

### 6. Get Incidents by District

**Endpoint**: `GET /api/incidents/district/{districtId}`

```bash
curl "http://localhost:8080/api/incidents/district/1?page=0&size=20"
```

### 7. Create Recommendation

**Endpoint**: `POST /api/recommendations`

```bash
curl -X POST http://localhost:8080/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "type": "Traffic Management",
    "priority": "HIGH",
    "message": "Implement traffic signal optimization during peak hours",
    "districtId": 1
  }'
```

**Expected Response** (201 Created):

```json
{
  "id": 1,
  "type": "Traffic Management",
  "priority": "HIGH",
  "message": "Implement traffic signal optimization during peak hours",
  "district_id": 1,
  "district_name": "Downtown District",
  "created_at": "2024-05-19T09:00:00",
  "updated_at": "2024-05-19T09:00:00"
}
```

### 8. Record Analytics Event

**Endpoint**: `POST /api/analytics/events`

```bash
curl -X POST http://localhost:8080/api/analytics/events \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Air Quality",
    "score": 68.5,
    "source": "Environmental Sensor Network",
    "metadata": "{\"location\": \"Downtown\", \"sensor_id\": \"AQ-001\"}"
  }'
```

**Expected Response** (201 Created):

```json
{
  "id": 1,
  "category": "Air Quality",
  "score": 68.5,
  "source": "Environmental Sensor Network",
  "timestamp": "2024-05-19T10:15:00",
  "metadata": "{\"location\": \"Downtown\", \"sensor_id\": \"AQ-001\"}"
}
```

### 9. Get Analytics Aggregates

**Endpoint**: `GET /api/analytics/aggregates/{category}`

```bash
curl "http://localhost:8080/api/analytics/aggregates/Air%20Quality"
```

**Expected Response**:

```json
{
  "total_events": 1,
  "average_score": 68.5,
  "highest_score": 68.5,
  "lowest_score": 68.5,
  "category": "Air Quality",
  "time_period": "ALL_TIME"
}
```

### 10. Get District Metrics (With Health Score)

**Endpoint**: `GET /api/districts/{id}/metrics`

```bash
curl http://localhost:8080/api/districts/1/metrics
```

**Expected Response**:

```json
{
  "id": 1,
  "name": "Downtown District",
  "sustainability_score": 72.5,
  "operational_risk_score": 45.3,
  "health_score": 63.6,
  "recent_incidents_count": 1
}
```

### 11. Update Incident

**Endpoint**: `PUT /api/incidents/{id}`

```bash
curl -X PUT http://localhost:8080/api/incidents/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "RESOLVED",
    "description": "Traffic congestion resolved after construction completion"
  }'
```

### 12. Delete District (Cascades)

**Endpoint**: `DELETE /api/districts/{id}`

```bash
curl -X DELETE http://localhost:8080/api/districts/1
# Returns 204 No Content
```

## Error Response Examples

### 404 Not Found

```bash
curl http://localhost:8080/api/districts/999
```

**Response**:

```json
{
  "status": 404,
  "message": "District not found with ID: 999",
  "error": "NOT_FOUND",
  "timestamp": "2024-05-19T10:35:00"
}
```

### 400 Bad Request (Validation)

```bash
curl -X POST http://localhost:8080/api/districts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "population": -1000,
    "sustainabilityScore": 150
  }'
```

**Response**:

```json
{
  "status": 400,
  "message": "Validation failed",
  "error": "VALIDATION_ERROR",
  "timestamp": "2024-05-19T10:35:00",
  "validation_errors": [
    {
      "field": "name",
      "message": "District name is required"
    },
    {
      "field": "population",
      "message": "Population must be positive"
    }
  ]
}
```

## Advanced Testing Scenarios

### Scenario 1: Complete Workflow

1. Create 5 districts
2. Create incidents in each district
3. Generate recommendations for high-risk districts
4. Query analytics aggregates

### Scenario 2: Pagination Testing

```bash
# First page
curl "http://localhost:8080/api/incidents?page=0&size=5"

# Second page
curl "http://localhost:8080/api/incidents?page=1&size=5"

# Custom sorting
curl "http://localhost:8080/api/incidents?page=0&size=5&sort=severity,desc"
```

### Scenario 3: Filter by Status

```bash
curl "http://localhost:8080/api/incidents/status/IN_PROGRESS"
curl "http://localhost:8080/api/incidents/status/RESOLVED"
```

### Scenario 4: Severity Filtering

```bash
curl "http://localhost:8080/api/incidents/severity/CRITICAL?page=0&size=20"
```

### Scenario 5: Active Incidents Query

```bash
curl "http://localhost:8080/api/incidents/district/1/active"
```

## Bulk Testing with Shell Scripts

### Create Multiple Districts

```bash
#!/bin/bash

DISTRICTS=("Downtown" "Midtown" "Uptown" "Waterfront" "Industrial")
POPULATIONS=(85000 65000 45000 35000 25000)
SUSTAINABILITY=(72.5 68.0 80.2 75.8 55.0)
RISK=(45.3 38.5 25.1 32.0 62.5)

for i in {0..4}; do
  curl -X POST http://localhost:8080/api/districts \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"${DISTRICTS[$i]}\",
      \"population\": ${POPULATIONS[$i]},
      \"sustainabilityScore\": ${SUSTAINABILITY[$i]},
      \"operationalRiskScore\": ${RISK[$i]}
    }"
  echo ""
done
```

### Create Multiple Incidents

```bash
#!/bin/bash

TYPES=("Traffic Congestion" "Power Outage" "Water Main Break" "Air Quality Alert" "Infrastructure Damage")
DESCRIPTIONS=("Heavy traffic on main streets" "Electrical outage" "Water pipe burst" "Pollution spike" "Road damage")
SEVERITIES=("HIGH" "CRITICAL" "MEDIUM" "HIGH" "MEDIUM")

for i in {0..4}; do
  curl -X POST http://localhost:8080/api/incidents \
    -H "Content-Type: application/json" \
    -d "{
      \"type\": \"${TYPES[$i]}\",
      \"description\": \"${DESCRIPTIONS[$i]}\",
      \"severity\": \"${SEVERITIES[$i]}\",
      \"latitude\": 40.7$(( RANDOM % 100 )),
      \"longitude\": -74.00$(( RANDOM % 100 )),
      \"districtId\": $(( 1 + RANDOM % 5 ))
    }"
  echo ""
done
```

## Response Time Testing

### Measure API Latency

```bash
# Using curl with time metrics
curl -w "Total: %{time_total}s, Connect: %{time_connect}s, Transfer: %{time_starttransfer}s\n" \
  http://localhost:8080/api/districts

# Using Apache Bench
ab -n 100 -c 10 http://localhost:8080/api/districts/

# Using wrk (load testing)
wrk -t4 -c100 -d30s http://localhost:8080/api/districts
```

## Monitoring During Testing

### In Another Terminal, Watch Logs

```bash
# Follow application logs
tail -f logs/urban-intelligence-platform.log

# Or using grep to filter
tail -f logs/urban-intelligence-platform.log | grep "IncidentController"
```

## Testing Checklist

- [ ] Health check passes
- [ ] Create district successfully
- [ ] Retrieve single district
- [ ] List districts with pagination
- [ ] Create incident in district
- [ ] Retrieve incidents by district
- [ ] Create recommendation
- [ ] Record analytics event
- [ ] Get analytics aggregates
- [ ] Get district metrics
- [ ] Update incident status
- [ ] Delete incident
- [ ] Delete district (cascade check)
- [ ] Verify 404 for non-existent resources
- [ ] Verify validation errors
- [ ] Test pagination edge cases
- [ ] Verify response times acceptable

## Performance Expectations

- Single resource GET: < 50ms
- List with pagination: < 100ms
- Create resource: < 150ms
- Update resource: < 100ms
- Delete resource: < 100ms
- Analytics aggregates: < 200ms

---

**Last Updated**: May 19, 2024  
**Test Coverage**: All REST endpoints  
**Recommended**: Automate tests with integration test suite
