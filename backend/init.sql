-- Urban Intelligence Platform - Database Initialization Script

-- Create UUID extension (for future use)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create districts table
CREATE TABLE IF NOT EXISTS districts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    population INTEGER NOT NULL,
    sustainability_score DOUBLE PRECISION NOT NULL,
    operational_risk_score DOUBLE PRECISION NOT NULL
);

-- Create incidents table
CREATE TABLE IF NOT EXISTS incidents (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    district_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE
);

-- Create recommendations table
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    auto_generated BOOLEAN NOT NULL DEFAULT FALSE,
    district_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE
);

-- Create analytics_events table
CREATE TABLE IF NOT EXISTS analytics_events (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    source VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    metadata TEXT
);

-- Create indexes
CREATE INDEX idx_incident_district ON incidents(district_id);
CREATE INDEX idx_incident_status ON incidents(status);
CREATE INDEX idx_incident_created_at ON incidents(created_at);
CREATE INDEX idx_recommendation_district ON recommendations(district_id);
CREATE INDEX idx_recommendation_priority ON recommendations(priority);
CREATE INDEX idx_recommendation_created_at ON recommendations(created_at);
CREATE INDEX idx_analytics_category ON analytics_events(category);
CREATE INDEX idx_analytics_source ON analytics_events(source);
CREATE INDEX idx_analytics_timestamp ON analytics_events(timestamp);

-- Insert sample data
INSERT INTO districts (name, population, sustainability_score, operational_risk_score)
VALUES 
    ('Downtown', 85000, 72.5, 45.3),
    ('Midtown', 65000, 68.0, 38.5),
    ('Uptown', 45000, 80.2, 25.1),
    ('Waterfront', 35000, 75.8, 32.0),
    ('Industrial', 25000, 55.0, 62.5)
ON CONFLICT (name) DO NOTHING;

INSERT INTO incidents (type, description, severity, latitude, longitude, district_id, status, created_at, updated_at)
VALUES
    ('Traffic Congestion', 'Heavy traffic on Main St', 'HIGH', 40.7128, -74.0060, 1, 'IN_PROGRESS', NOW(), NOW()),
    ('Power Outage', 'Electrical outage affecting 3 blocks', 'CRITICAL', 40.7589, -73.9851, 2, 'REPORTED', NOW(), NOW()),
    ('Water Leak', 'Major water leak detected', 'MEDIUM', 40.7489, -73.9680, 1, 'RESOLVED', NOW(), NOW())
ON CONFLICT DO NOTHING;
