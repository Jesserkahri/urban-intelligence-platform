-- Phase 6 - Predictive Intelligence

CREATE TABLE IF NOT EXISTS predictive_alerts (
    id BIGSERIAL PRIMARY KEY,
    district_id BIGINT,
    alert_type VARCHAR(80) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    probability DOUBLE PRECISION NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    forecast_window_days INTEGER NOT NULL,
    predicted_value DOUBLE PRECISION,
    baseline_value DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_predictive_alert_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE SET NULL
);

ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS predicted_impact DOUBLE PRECISION;
ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS intervention_effectiveness DOUBLE PRECISION;
ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS operational_confidence DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_predictive_alert_district ON predictive_alerts(district_id);
CREATE INDEX IF NOT EXISTS idx_predictive_alert_type ON predictive_alerts(alert_type);
CREATE INDEX IF NOT EXISTS idx_predictive_alert_severity ON predictive_alerts(severity);
CREATE INDEX IF NOT EXISTS idx_predictive_alert_created ON predictive_alerts(created_at);
CREATE INDEX IF NOT EXISTS idx_recommendation_confidence ON recommendations(operational_confidence);
CREATE INDEX IF NOT EXISTS idx_recommendation_predicted_impact ON recommendations(predicted_impact);
