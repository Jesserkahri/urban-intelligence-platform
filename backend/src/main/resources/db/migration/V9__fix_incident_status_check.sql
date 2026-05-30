-- Fix incident status CHECK constraint
-- Hibernate auto-generates CHECK constraints for @Enumerated(EnumType.STRING) columns.
-- When the IncidentStatus enum was changed from REPORTED to OPEN, the old constraint
-- CHECK (status IN ('REPORTED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
-- blocked inserts/updates with the new 'OPEN' value.
--
-- This migration drops the old constraint and recreates it with the correct values.

DO $$
BEGIN
    -- Drop the Hibernate-generated CHECK constraint if it exists
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'incidents_status_check'
        AND connamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public')
    ) THEN
        ALTER TABLE incidents DROP CONSTRAINT incidents_status_check;
        RAISE NOTICE 'Dropped old incidents_status_check constraint';
    END IF;

    -- Add the corrected CHECK constraint
    -- Note: Hibernate will NOT re-generate this since ddl-auto=validate
    ALTER TABLE incidents ADD CONSTRAINT incidents_status_check
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'));
    RAISE NOTICE 'Added corrected incidents_status_check constraint';
END $$;