-- V3: Enforce auto-generated recommendation deduplication under concurrency.
-- Existing duplicate generated rows are collapsed before adding the partial unique index.

DELETE FROM recommendations r
USING recommendations duplicate
WHERE r.id > duplicate.id
  AND r.auto_generated = true
  AND duplicate.auto_generated = true
  AND r.content_hash IS NOT NULL
  AND r.content_hash = duplicate.content_hash;

CREATE UNIQUE INDEX IF NOT EXISTS uk_recommendation_auto_content_hash
    ON recommendations(content_hash)
    WHERE auto_generated = true AND content_hash IS NOT NULL;
