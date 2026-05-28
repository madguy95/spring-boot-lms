-- ========================================
-- V12__Class_Lifecycle_And_Editing_Policy.sql
-- ========================================
-- Promotes the class status model to the new admin-controlled lifecycle:
--   {draft, published, unpublished, cancelled} stored;
--   {open, full, ongoing, completed} derived from lifecycle + dates + capacity.
--
-- DB is empty per design discussion, so no data backfill needed for the
-- old {running, upcoming, ended} values.
-- ========================================

-- Drop the previous CHECK before renaming so a transient state never violates it.
ALTER TABLE classes DROP CONSTRAINT IF EXISTS chk_classes_status;

ALTER TABLE classes RENAME COLUMN status TO lifecycle_status;

ALTER TABLE classes
    ALTER COLUMN lifecycle_status SET DEFAULT 'draft';

ALTER TABLE classes
    ADD CONSTRAINT chk_classes_lifecycle_status
    CHECK (lifecycle_status IN ('draft', 'published', 'unpublished', 'cancelled'));

-- Reason text shown alongside CANCELLED classes (audit trail for parents/teachers).
ALTER TABLE classes
    ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(500);

-- current_session_index was an attempt to track progress, but it's derivable
-- from (start_date, end_date, daySchedules count). Drop it to avoid drift.
ALTER TABLE classes
    DROP COLUMN IF EXISTS current_session_index;

-- Rename the index too so it matches the new column name.
DROP INDEX IF EXISTS idx_classes_status;
CREATE INDEX IF NOT EXISTS idx_classes_lifecycle_status ON classes(lifecycle_status);
