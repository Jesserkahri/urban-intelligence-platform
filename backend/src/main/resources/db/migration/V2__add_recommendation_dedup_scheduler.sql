-- V2: Add recommendation dedup support + cleanup scheduler indexes
-- Adds content_hash column for deduplication of auto-generated recommendations.

ALTER TABLE recommendations ADD COLUMN IF NOT EXISTS content_hash VARCHAR(64);

-- Index for fast dedup lookup
CREATE INDEX IF NOT EXISTS idx_recommendation_content_hash ON recommendations(content_hash);

-- Index for cleanup job (purging old auto-generated recommendations)
CREATE INDEX IF NOT EXISTS idx_recommendation_auto_gen_created ON recommendations(auto_generated, created_at);

-- Index for refresh token cleanup
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires ON refresh_token_sessions(expires_at);

-- Index for analytics event cleanup
CREATE INDEX IF NOT EXISTS idx_analytics_event_timestamp ON analytics_events(timestamp);