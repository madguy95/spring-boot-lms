-- ========================================
-- V21__Create_Class_Session_Attendance_Notes_Schema.sql
-- ========================================
-- Backs the Teacher "Class detail" screen:
--   class_session       — the taught/upcoming sessions of a class (B1..Bn)
--   class_attendance    — one row per (session, enrolled student) attendance mark
--   class_session_note  — one row per (session, enrolled student) review note
--                         (+ the per-session whole-class summary lives on
--                          class_session.summary_comment / summary_rating)
--
-- Students are referenced via enrollments(id) — the same roster source the
-- "My classes" avatar stacks and the class-detail student list already use
-- (only enrollments.status = 'active' belong to a class roster).
-- ========================================

CREATE TABLE IF NOT EXISTS class_session (
    id                BIGSERIAL    PRIMARY KEY,
    class_id          BIGINT       NOT NULL,
    idx               INTEGER      NOT NULL,            -- 1-based position within the class
    code              VARCHAR(20)  NOT NULL,            -- short label, e.g. 'B6'
    session_date      DATE,
    date_label        VARCHAR(40),                      -- e.g. '19/05'
    title             VARCHAR(200),
    description       TEXT,
    status            VARCHAR(20)  NOT NULL DEFAULT 'upcoming',
    time_label        VARCHAR(80),                      -- e.g. '18:00–19:00 · Phòng 301'
    day_label         VARCHAR(80),                      -- e.g. 'T2 · 19/05/2026 · 18:00–19:00'
    summary_comment   TEXT,
    summary_rating    VARCHAR(20),

    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),
    version           BIGINT       DEFAULT 0,

    CONSTRAINT fk_class_session_class
        FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT uq_class_session_class_code
        UNIQUE (class_id, code),
    CONSTRAINT chk_class_session_status
        CHECK (status IN ('reviewed', 'in_progress', 'taught', 'upcoming')),
    CONSTRAINT chk_class_session_summary_rating
        CHECK (summary_rating IS NULL OR summary_rating IN ('weak','average','good','great','excellent'))
);

CREATE INDEX IF NOT EXISTS idx_class_session_class ON class_session(class_id);

CREATE TABLE IF NOT EXISTS class_attendance (
    id                BIGSERIAL    PRIMARY KEY,
    session_id        BIGINT       NOT NULL,
    enrollment_id     BIGINT       NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'unmarked',
    note              VARCHAR(255),

    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),
    version           BIGINT       DEFAULT 0,

    CONSTRAINT fk_class_attendance_session
        FOREIGN KEY (session_id) REFERENCES class_session(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_attendance_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
    CONSTRAINT uq_class_attendance_session_student
        UNIQUE (session_id, enrollment_id),
    CONSTRAINT chk_class_attendance_status
        CHECK (status IN ('present', 'excused', 'absent', 'makeup', 'unmarked'))
);

CREATE INDEX IF NOT EXISTS idx_class_attendance_session ON class_attendance(session_id);

CREATE TABLE IF NOT EXISTS class_session_note (
    id                BIGSERIAL    PRIMARY KEY,
    session_id        BIGINT       NOT NULL,
    enrollment_id     BIGINT       NOT NULL,
    attendance        VARCHAR(20)  NOT NULL DEFAULT 'present',
    note              TEXT,
    rating            VARCHAR(20),
    tags              VARCHAR(255),                      -- comma-separated skill tags
    saved             BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),
    version           BIGINT       DEFAULT 0,

    CONSTRAINT fk_class_session_note_session
        FOREIGN KEY (session_id) REFERENCES class_session(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_session_note_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
    CONSTRAINT uq_class_session_note_session_student
        UNIQUE (session_id, enrollment_id),
    CONSTRAINT chk_class_session_note_attendance
        CHECK (attendance IN ('present', 'excused', 'absent', 'makeup', 'unmarked')),
    CONSTRAINT chk_class_session_note_rating
        CHECK (rating IS NULL OR rating IN ('weak','average','good','great','excellent'))
);

CREATE INDEX IF NOT EXISTS idx_class_session_note_session ON class_session_note(session_id);
