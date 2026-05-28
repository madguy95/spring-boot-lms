-- ========================================
-- V16__Seed_MasterData_Subjects_Locations.sql
-- ========================================
-- Seed master_data rows for the two lookup types used by the Create Teacher
-- form: `subject` (primary subject dropdown) and `location` (location dropdown).
--
-- Subject codes mirror the names already seeded into `subjects` in V8 so the
-- existing `primarySubjectId` FK lookup in CourseService keeps working without
-- a schema change — the FE form sends the master_data code (= subject name)
-- and the service resolves it to a subjects.id via name match.
-- ========================================

INSERT INTO master_data (type, code, name, position, active) VALUES
    ('subject', 'Scratch',      'Scratch',       1, TRUE),
    ('subject', 'Python',       'Python',        2, TRUE),
    ('subject', 'JavaScript',   'JavaScript',    3, TRUE),
    ('subject', 'Web Design',   'Web Design',    4, TRUE),
    ('subject', 'UI/UX',        'UI/UX',         5, TRUE),
    ('subject', 'Robotics',     'Robotics',      6, TRUE),
    ('subject', 'Arduino',      'Arduino',       7, TRUE),
    ('subject', 'Game Dev',     'Game Dev',      8, TRUE),
    ('subject', 'Unity',        'Unity',         9, TRUE),
    ('subject', 'Math',         'Math',         10, TRUE),
    ('subject', 'English',      'English',      11, TRUE),
    ('subject', 'AI Explorers', 'AI Explorers', 12, TRUE)
ON CONFLICT (type, code) DO NOTHING;

INSERT INTO master_data (type, code, name, position, active) VALUES
    ('location', 'Hanoi · Vinsmart', 'Hà Nội · Vinsmart', 1, TRUE),
    ('location', 'Hanoi · Cầu Giấy', 'Hà Nội · Cầu Giấy', 2, TRUE),
    ('location', 'Hanoi · Thanh Xuân','Hà Nội · Thanh Xuân', 3, TRUE),
    ('location', 'HCM · Quận 1',     'TP. HCM · Quận 1',  4, TRUE),
    ('location', 'HCM · Quận 7',     'TP. HCM · Quận 7',  5, TRUE),
    ('location', 'Online only',      'Online only',       6, TRUE)
ON CONFLICT (type, code) DO NOTHING;
