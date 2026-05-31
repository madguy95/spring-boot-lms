-- ============================================================================
-- V23  Seed sample taught sessions for every class so the teacher
--      "Class detail" screen (attendance / notes tabs) has content to work with.
-- ----------------------------------------------------------------------------
-- 8 sessions per class, anchored around today: B1..B5 reviewed, B6 in-progress
-- (this becomes the "current" session the attendance/notes tabs open on),
-- B7..B8 upcoming. Idempotent via ON CONFLICT on uq_class_session_class_code.
-- ============================================================================

INSERT INTO class_session
    (class_id, idx, code, session_date, date_label, title, description, status,
     time_label, day_label, created_at, updated_at, version)
SELECT
    c.id,
    g.idx,
    'B' || g.idx,
    (CURRENT_DATE + ((g.idx - 6) * 7))::date,
    to_char(CURRENT_DATE + ((g.idx - 6) * 7), 'DD/MM'),
    'Buổi ' || g.idx,
    CASE WHEN g.idx = 6 THEN 'Buổi học hiện tại — điểm danh & nhận xét.' ELSE NULL END,
    CASE WHEN g.idx <= 5 THEN 'reviewed'
         WHEN g.idx = 6 THEN 'in_progress'
         ELSE 'upcoming' END,
    '18:00–19:00' || COALESCE(' · ' || NULLIF(c.room, ''), ''),
    to_char(CURRENT_DATE + ((g.idx - 6) * 7), 'DD/MM/YYYY') || ' · 18:00–19:00',
    NOW(), NOW(), 0
FROM classes c
CROSS JOIN generate_series(1, 8) AS g(idx)
ON CONFLICT (class_id, code) DO NOTHING;
