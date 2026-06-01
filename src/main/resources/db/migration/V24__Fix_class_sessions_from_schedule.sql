-- ============================================================================
-- V24  Regenerate class_session rows from each class's actual day schedule.
-- ----------------------------------------------------------------------------
-- V23 seeded sessions with CURRENT_DATE ± N×7 (hardcoded weekly offsets),
-- ignoring class_day_schedules entirely. This caused every session to fall
-- on the same weekday as the migration ran, regardless of the class schedule.
-- time_label was also hardcoded to '18:00–19:00' instead of using each
-- class's startTime/endTime, and day_label was missing the Vietnamese
-- day-of-week prefix (T2, T3, …).
--
-- This migration:
--   1. Deletes all existing sessions (class_attendance and class_session_note
--      cascade automatically via ON DELETE CASCADE).
--   2. Regenerates sessions by walking each class's [start_date, end_date]
--      range and picking only dates whose weekday matches a class_day_schedules
--      row, ordered chronologically then by schedule position within a day.
--   3. Derives date_label, day_label (with Vietnamese prefix), time_label,
--      and status from the real schedule and the date vs CURRENT_DATE.
-- ============================================================================

DELETE FROM class_session;

WITH
schedule AS (
    SELECT
        cds.class_id,
        cds.position,
        cds.start_time,
        cds.end_time,
        CASE cds.day
            WHEN 'Sun' THEN 0
            WHEN 'Mon' THEN 1
            WHEN 'Tue' THEN 2
            WHEN 'Wed' THEN 3
            WHEN 'Thu' THEN 4
            WHEN 'Fri' THEN 5
            WHEN 'Sat' THEN 6
        END AS dow,
        CASE cds.day
            WHEN 'Sun' THEN 'CN'
            WHEN 'Mon' THEN 'T2'
            WHEN 'Tue' THEN 'T3'
            WHEN 'Wed' THEN 'T4'
            WHEN 'Thu' THEN 'T5'
            WHEN 'Fri' THEN 'T6'
            WHEN 'Sat' THEN 'T7'
        END AS day_vn
    FROM class_day_schedules cds
),
candidate AS (
    SELECT
        c.id              AS class_id,
        c.total_sessions,
        c.room,
        s.position,
        s.start_time,
        s.end_time,
        s.day_vn,
        gs.d::date        AS session_date
    FROM classes c
    JOIN schedule s ON s.class_id = c.id
    CROSS JOIN LATERAL generate_series(c.start_date, c.end_date, '1 day'::interval) AS gs(d)
    WHERE EXTRACT(DOW FROM gs.d) = s.dow
),
ranked AS (
    SELECT *,
        ROW_NUMBER() OVER (
            PARTITION BY class_id
            ORDER BY session_date, position
        ) AS idx
    FROM candidate
),
limited AS (
    SELECT * FROM ranked
    WHERE idx <= COALESCE(total_sessions, 9999)
)
INSERT INTO class_session
    (class_id, idx, code, session_date, date_label, day_label, time_label,
     title, status, created_at, updated_at, version)
SELECT
    class_id,
    idx,
    'B' || idx,
    session_date,
    to_char(session_date, 'DD/MM'),
    -- e.g. 'T2 · 19/05/2026 · 18:00–19:00'
    day_vn || ' · ' || to_char(session_date, 'DD/MM/YYYY')
        || ' · ' || start_time || '–' || end_time,
    -- e.g. '18:00–19:00 · Phòng 301'
    start_time || '–' || end_time
        || COALESCE(' · ' || NULLIF(TRIM(room), ''), ''),
    'Buổi ' || idx,
    CASE
        WHEN session_date < CURRENT_DATE THEN 'reviewed'
        WHEN session_date = CURRENT_DATE THEN 'in_progress'
        ELSE 'upcoming'
    END,
    NOW(), NOW(), 0
FROM limited;
