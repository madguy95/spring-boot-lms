-- ========================================
-- V14__Course_Rename_Category_To_Tool_Drop_Level.sql
-- ========================================
-- Refactor: rename courses.category -> courses.tool with values now sourced
-- from master_data (type='tool'); drop courses.level entirely. Old CHECK
-- constraints removed since validation lives in CourseService via
-- MasterDataRepository lookup rather than a hardcoded SQL list.
-- ========================================

ALTER TABLE courses DROP CONSTRAINT IF EXISTS chk_courses_category;
ALTER TABLE courses DROP CONSTRAINT IF EXISTS chk_courses_level;

DROP INDEX IF EXISTS idx_courses_category;

ALTER TABLE courses RENAME COLUMN category TO tool;
ALTER TABLE courses ALTER COLUMN tool TYPE VARCHAR(50);

ALTER TABLE courses DROP COLUMN level;

CREATE INDEX IF NOT EXISTS idx_courses_tool ON courses(tool);
