-- ========================================
-- V11__Create_Class_Schema.sql
-- ========================================
-- Class module: course instances scheduled for a teacher, with one weekly
-- recurring schedule per day in a separate table (1:N).
-- `name` and `schedule` are denormalized for fast list rendering; the source
-- of truth is `course_id` + `class_day_schedules` rows.
-- ========================================

CREATE TABLE IF NOT EXISTS classes (
    id                       BIGSERIAL    PRIMARY KEY,
    name                     VARCHAR(200) NOT NULL,
    label                    VARCHAR(50)  NOT NULL,
    course_id                BIGINT       NOT NULL,
    teacher_id               BIGINT       NOT NULL,
    location                 VARCHAR(120) NOT NULL,
    schedule                 VARCHAR(200) NOT NULL,
    start_date               DATE         NOT NULL,
    end_date                 DATE         NOT NULL,
    capacity                 INTEGER      NOT NULL DEFAULT 0,
    enrolled                 INTEGER      NOT NULL DEFAULT 0,
    visibility               VARCHAR(30)  NOT NULL DEFAULT 'public_enrollable',
    status                   VARCHAR(20)  NOT NULL DEFAULT 'upcoming',
    current_session_index    INTEGER,
    total_sessions           INTEGER,
    created_at               TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(50),
    updated_by               VARCHAR(50),
    version                  BIGINT       DEFAULT 0,

    CONSTRAINT fk_classes_course
        FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_classes_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers(id),
    CONSTRAINT chk_classes_status
        CHECK (status IN ('running', 'upcoming', 'ended')),
    CONSTRAINT chk_classes_visibility
        CHECK (visibility IN ('public_enrollable', 'public_view', 'private')),
    CONSTRAINT chk_classes_capacity     CHECK (capacity >= 0),
    CONSTRAINT chk_classes_enrolled     CHECK (enrolled >= 0),
    CONSTRAINT chk_classes_date_range   CHECK (end_date >= start_date)
);

CREATE INDEX IF NOT EXISTS idx_classes_status     ON classes(status);
CREATE INDEX IF NOT EXISTS idx_classes_course_id  ON classes(course_id);
CREATE INDEX IF NOT EXISTS idx_classes_teacher_id ON classes(teacher_id);

-- class_day_schedules — one row per weekly recurring slot (1:N).
-- `day` is a short label (Sun/Mon/.../Sat) to match the FE schema; times stored
-- as strings preserve the user's "HH:mm" representation without timezone games.
CREATE TABLE IF NOT EXISTS class_day_schedules (
    id          BIGSERIAL   PRIMARY KEY,
    class_id    BIGINT      NOT NULL,
    position    INTEGER     NOT NULL,
    day         VARCHAR(3)  NOT NULL,
    start_time  VARCHAR(5)  NOT NULL,
    end_time    VARCHAR(5)  NOT NULL,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_class_day_schedules_class
        FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT uq_class_day_schedules_position UNIQUE (class_id, position),
    CONSTRAINT chk_class_day_schedules_day
        CHECK (day IN ('Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'))
);

CREATE INDEX IF NOT EXISTS idx_class_day_schedules_class ON class_day_schedules(class_id);
