-- Phase 5 - Real-Time Operations notification center

CREATE TABLE IF NOT EXISTS operational_notifications (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(80) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    incident_id BIGINT,
    district_id BIGINT,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notification_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE SET NULL,
    CONSTRAINT fk_notification_district FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_severity ON operational_notifications(severity);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON operational_notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notification_read ON operational_notifications(read_at);
CREATE INDEX IF NOT EXISTS idx_notification_district ON operational_notifications(district_id);
CREATE INDEX IF NOT EXISTS idx_notification_ack_created ON operational_notifications(acknowledged, created_at);
CREATE INDEX IF NOT EXISTS idx_incident_created_severity ON incidents(created_at, severity);
CREATE INDEX IF NOT EXISTS idx_incident_status_created ON incidents(status, created_at);
