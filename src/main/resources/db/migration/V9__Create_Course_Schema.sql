-- ========================================
-- V9__Create_Course_Schema.sql
-- ========================================
-- Course module: course catalog with outline (sessions) and discount rules.
-- File fields (cover_url, intro_video_url) reserved for future storage integration.
-- ========================================

CREATE TABLE IF NOT EXISTS courses (
    id                       BIGSERIAL    PRIMARY KEY,
    code                     VARCHAR(50)  NOT NULL UNIQUE,
    title                    VARCHAR(200) NOT NULL,
    tagline                  VARCHAR(200),
    description              TEXT,
    category                 VARCHAR(20)  NOT NULL,
    level                    VARCHAR(20)  NOT NULL,
    status                   VARCHAR(20)  NOT NULL DEFAULT 'draft',
    min_age                  INTEGER      NOT NULL,
    max_age                  INTEGER      NOT NULL,
    total_sessions           INTEGER      NOT NULL DEFAULT 0,
    session_duration_minutes INTEGER      NOT NULL DEFAULT 0,
    per_class_capacity       INTEGER      NOT NULL DEFAULT 0,
    tuition_amount           NUMERIC(15,2) NOT NULL DEFAULT 0,
    original_tuition_amount  NUMERIC(15,2),
    cover_url                VARCHAR(500),                    -- populated once storage integration lands
    intro_video_url          VARCHAR(500),                    -- populated once storage integration lands
    pricing_notes            TEXT,
    version_label            VARCHAR(20)  NOT NULL DEFAULT 'v0.1',  -- user-facing semver-like tag, distinct from optimistic-lock `version`
    created_at               TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(50),
    updated_by               VARCHAR(50),
    version                  BIGINT       DEFAULT 0,

    CONSTRAINT chk_courses_status    CHECK (status IN ('published', 'draft')),
    CONSTRAINT chk_courses_category  CHECK (category IN ('coding', 'design', 'robotics', 'stem', 'language', 'game')),
    CONSTRAINT chk_courses_level     CHECK (level IN ('beginner', 'intermediate', 'advanced')),
    CONSTRAINT chk_courses_age_range CHECK (min_age >= 0 AND max_age >= min_age)
);

CREATE INDEX IF NOT EXISTS idx_courses_status   ON courses(status);
CREATE INDEX IF NOT EXISTS idx_courses_category ON courses(category);

-- course_sessions — ordered outline items per course (1:N).
CREATE TABLE IF NOT EXISTS course_sessions (
    id          BIGSERIAL    PRIMARY KEY,
    course_id   BIGINT       NOT NULL,
    position    INTEGER      NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_course_sessions_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uq_course_sessions_position UNIQUE (course_id, position)
);

CREATE INDEX IF NOT EXISTS idx_course_sessions_course ON course_sessions(course_id);

-- course_discounts — promotion/discount rules per course (1:N).
-- value_numeric is used for percentage/fixed; value_text is used for special.
CREATE TABLE IF NOT EXISTS course_discounts (
    id             BIGSERIAL     PRIMARY KEY,
    course_id      BIGINT        NOT NULL,
    position       INTEGER       NOT NULL,
    name           VARCHAR(120)  NOT NULL,
    type           VARCHAR(20)   NOT NULL,
    value_numeric  NUMERIC(15,2),
    value_text     VARCHAR(255),
    condition      VARCHAR(30)   NOT NULL DEFAULT 'none',
    condition_date DATE,
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_course_discounts_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT chk_course_discounts_type
        CHECK (type IN ('percentage', 'fixed', 'special')),
    CONSTRAINT chk_course_discounts_condition
        CHECK (condition IN ('none', 'before_date', 'has_sibling', 'trial_only'))
);

CREATE INDEX IF NOT EXISTS idx_course_discounts_course ON course_discounts(course_id);
