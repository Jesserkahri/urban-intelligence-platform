# Urban Intelligence Platform

Urban Intelligence Platform is a full-stack civic analytics application that combines a Spring Boot backend with a React + TypeScript frontend for managing districts, incidents, recommendations, sustainability metrics, and predictive insights.

## Overview

This repository now contains both the API layer and the web application layer:

- Backend: Java 21, Spring Boot 3.5, Spring Security, PostgreSQL, Flyway, JWT authentication, OpenAPI/Swagger
- Frontend: React 18, TypeScript, Vite, Tailwind CSS, React Query, Leaflet, Recharts
- Dev workflow: Docker Compose for PostgreSQL and the application container

## Current Project Structure

```text
Urban flagship/
├── backend/
│   ├── src/main/java/com/urban/intelligence/platform/
│   │   ├── api/controller/
│   │   ├── auth/api/
│   │   ├── analytics/
│   │   ├── config/
│   │   ├── domain/
│   │   └── service/
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   ├── components/
│   │   ├── features/
│   │   ├── layouts/
│   │   ├── pages/
│   │   └── services/
│   ├── package.json
│   └── vite.config.ts
├── docker-compose.yml
└── README.md
```

## Key Features

- Authentication and authorization foundation with JWT
- District and incident management
- Recommendation and sustainability workflows
- Analytics dashboards with hotspot, trend, and risk insights
- Predictive intelligence and operational overview endpoints
- Spatial and map-based views for incident exploration

## Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 18+
- npm 9+
- Docker Desktop (optional, recommended)
- PostgreSQL 16+ or Docker Compose

## Quick Start

### Option 1: Run everything with Docker Compose

```bash
docker compose up --build
```

This starts:

- PostgreSQL on port 5432
- The backend API on port 8080

Environment variables are read from the compose file, and the backend expects a JWT secret to be provided.

### Option 2: Run backend locally

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The API will be available at:

- http://localhost:8080
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI docs: http://localhost:8080/api/docs

### Option 3: Run frontend locally

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at:

- http://localhost:5173

## Environment Variables

The backend uses environment-based configuration. Common variables include:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/urban_platform
SPRING_DATASOURCE_USERNAME=urban
SPRING_DATASOURCE_PASSWORD=urban_secret
JWT_SECRET=change-this-secret
JWT_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000
```

## Main API Areas

The backend currently exposes endpoints under these domains:

- Auth: /api/auth
- Districts: /api/districts
- Incidents: /api/incidents
- Recommendations: /api/recommendations
- Analytics: /api/analytics
- Sustainability: /api/sustainability
- Predictive intelligence: /api/predictive
- Operations: /api/operations

## Frontend Routes

The web app currently includes routes for:

- /login
- /dashboard
- /incidents
- /analytics
- /spatial
- /sustainability
- /recommendations
- /districts

## Testing

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
cd frontend
npm run build
npm run typecheck
npm run lint
```

## Development Notes

- The backend uses Flyway migrations for database evolution.
- The frontend is organized by feature folders for scalability.
- API documentation is available through Swagger UI once the backend is running.
- The project is designed to support future AI/ML and real-time analytics expansion.

## License

This project is currently maintained as an internal development workspace. Add an explicit license file before public distribution.

---

Version: 1.1.0
Status: Active development
