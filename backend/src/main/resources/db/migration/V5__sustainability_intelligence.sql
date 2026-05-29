-- Phase 4 - Sustainability Intelligence

CREATE TABLE IF NOT EXISTS sustainability_metrics (
    id BIGSERIAL PRIMARY KEY,
    district_id BIGINT NOT NULL,
    metric_type VARCHAR(100) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50) NOT NULL,
    threshold DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    source TEXT,
    CONSTRAINT fk_sustainability_metric_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sustainability_scores (
    id BIGSERIAL PRIMARY KEY,
    district_id BIGINT NOT NULL,
    overall_score DOUBLE PRECISION NOT NULL,
    environmental_score DOUBLE PRECISION NOT NULL,
    mobility_score DOUBLE PRECISION NOT NULL,
    energy_score DOUBLE PRECISION NOT NULL,
    waste_score DOUBLE PRECISION NOT NULL,
    rating VARCHAR(5) NOT NULL,
    trend VARCHAR(20) NOT NULL,
    trend_percentage DOUBLE PRECISION NOT NULL,
    calculated_at TIMESTAMP NOT NULL,
    previous_calculation TIMESTAMP NOT NULL,
    CONSTRAINT fk_sustainability_score_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sustainability_district ON sustainability_metrics(district_id);
CREATE INDEX IF NOT EXISTS idx_sustainability_metric_type ON sustainability_metrics(metric_type);
CREATE INDEX IF NOT EXISTS idx_sustainability_timestamp ON sustainability_metrics(timestamp);
CREATE INDEX IF NOT EXISTS idx_sustainability_status ON sustainability_metrics(status);
CREATE INDEX IF NOT EXISTS idx_sustainability_metric_type_timestamp ON sustainability_metrics(metric_type, timestamp);
CREATE INDEX IF NOT EXISTS idx_score_district ON sustainability_scores(district_id);
CREATE INDEX IF NOT EXISTS idx_score_timestamp ON sustainability_scores(calculated_at);
CREATE INDEX IF NOT EXISTS idx_score_rating ON sustainability_scores(rating);
CREATE INDEX IF NOT EXISTS idx_score_trend ON sustainability_scores(trend);

INSERT INTO sustainability_metrics (district_id, metric_type, value, unit, threshold, status, timestamp, source)
SELECT id, 'AIR_QUALITY', 42.0, 'AQI', 100.0, 'GOOD', NOW() - INTERVAL '5 days', 'sample-sensor-network'
FROM districts WHERE name = 'Downtown'
ON CONFLICT DO NOTHING;

INSERT INTO sustainability_metrics (district_id, metric_type, value, unit, threshold, status, timestamp, source)
SELECT id, 'EMISSIONS', 78.0, 'kg CO2e', 100.0, 'POOR', NOW() - INTERVAL '4 days', 'sample-emissions-model'
FROM districts WHERE name = 'Industrial'
ON CONFLICT DO NOTHING;

INSERT INTO sustainability_metrics (district_id, metric_type, value, unit, threshold, status, timestamp, source)
SELECT id, 'CONGESTION', 64.0, '%', 85.0, 'POOR', NOW() - INTERVAL '3 days', 'sample-mobility-feed'
FROM districts WHERE name = 'Midtown'
ON CONFLICT DO NOTHING;

INSERT INTO sustainability_metrics (district_id, metric_type, value, unit, threshold, status, timestamp, source)
SELECT id, 'MOBILITY_FLOW', 82.0, 'score', 75.0, 'GOOD', NOW() - INTERVAL '2 days', 'sample-mobility-feed'
FROM districts WHERE name = 'Waterfront'
ON CONFLICT DO NOTHING;

INSERT INTO sustainability_metrics (district_id, metric_type, value, unit, threshold, status, timestamp, source)
SELECT id, 'WASTE_GENERATION', 92.0, 'tons', 80.0, 'CRITICAL', NOW() - INTERVAL '1 day', 'sample-waste-ops'
FROM districts WHERE name = 'Industrial'
ON CONFLICT DO NOTHING;
