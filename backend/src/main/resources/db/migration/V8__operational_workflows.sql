-- Phase 2 - Operational workflow completion

ALTER TABLE incidents ADD COLUMN IF NOT EXISTS assigned_to VARCHAR(120);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS acknowledged BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS reviewed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS review_notes TEXT;

ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(120);
ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;
ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS review_notes TEXT;

CREATE TABLE IF NOT EXISTS activity_events (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(40) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(80) NOT NULL,
    actor VARCHAR(120),
    details TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_activity_entity ON activity_events(entity_type, entity_id, created_at);
CREATE INDEX IF NOT EXISTS idx_activity_created_at ON activity_events(created_at);
CREATE INDEX IF NOT EXISTS idx_incident_assigned_to ON incidents(assigned_to);
CREATE INDEX IF NOT EXISTS idx_incident_ack_review ON incidents(acknowledged, reviewed);
CREATE INDEX IF NOT EXISTS idx_recommendation_status ON recommendations(status);
