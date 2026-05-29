-- ========================================
-- V20__Enrollment_Add_Intended_Class.sql
-- ========================================
-- Workshop quick-signup needs to capture which specific class (workshop session)
-- the parent picked from the blog detail page. The existing `assigned_class_id`
-- is admin-controlled and only filled after approval, so we add a separate
-- `intended_class_id` that records the parent's pick at submission time.
--
-- Channel 'website_workshop' identifies these guest-flow submissions in the
-- admin enrollment queue so they can be triaged differently from full
-- consultation requests.
-- ========================================

ALTER TABLE enrollments
    ADD COLUMN intended_class_id BIGINT;

ALTER TABLE enrollments
    ADD CONSTRAINT fk_enrollments_intended_class
        FOREIGN KEY (intended_class_id) REFERENCES classes(id);

CREATE INDEX IF NOT EXISTS idx_enrollments_intended_class
    ON enrollments(intended_class_id);

-- Loosen channel CHECK to allow the new guest source. Postgres requires
-- drop+recreate for CHECK constraints.
ALTER TABLE enrollments DROP CONSTRAINT IF EXISTS chk_enrollments_channel;
ALTER TABLE enrollments
    ADD CONSTRAINT chk_enrollments_channel
        CHECK (channel IN ('parent_app', 'website', 'referral', 'website_workshop'));
