-- Urban Intelligence Platform - Initial Schema
-- Core tables for districts, incidents, recommendations, analytics, users, and sessions.

CREATE TABLE IF NOT EXISTS districts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    population INTEGER NOT NULL,
    sustainability_score DOUBLE PRECISION NOT NULL,
    operational_risk_score DOUBLE PRECISION NOT NULL
);

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
    CONSTRAINT fk_incident_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    auto_generated BOOLEAN NOT NULL DEFAULT FALSE,
    district_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_recommendation_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS analytics_events (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    source VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    metadata TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'VIEWER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(36),
    email_verification_token_expiry TIMESTAMP,
    password_reset_token VARCHAR(36),
    password_reset_token_expiry TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS refresh_token_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    device_info VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_incident_district ON incidents(district_id);
CREATE INDEX IF NOT EXISTS idx_incident_status ON incidents(status);
CREATE INDEX IF NOT EXISTS idx_incident_created_at ON incidents(created_at);
CREATE INDEX IF NOT EXISTS idx_recommendation_district ON recommendations(district_id);
CREATE INDEX IF NOT EXISTS idx_recommendation_priority ON recommendations(priority);
CREATE INDEX IF NOT EXISTS idx_recommendation_created_at ON recommendations(created_at);
CREATE INDEX IF NOT EXISTS idx_analytics_category ON analytics_events(category);
CREATE INDEX IF NOT EXISTS idx_analytics_source ON analytics_events(source);
CREATE INDEX IF NOT EXISTS idx_analytics_timestamp ON analytics_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_session_token ON refresh_token_sessions(token);
CREATE INDEX IF NOT EXISTS idx_session_user ON refresh_token_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);