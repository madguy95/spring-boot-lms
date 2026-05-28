-- ========================================
-- V10__Course_Add_Unpublished_Status.sql
-- ========================================
-- Extends the course state machine: draft → published → unpublished.
-- Existing 'published'/'draft' rows are unaffected.
-- ========================================

ALTER TABLE courses DROP CONSTRAINT IF EXISTS chk_courses_status;
ALTER TABLE courses
    ADD CONSTRAINT chk_courses_status
    CHECK (status IN ('published', 'draft', 'unpublished'));
