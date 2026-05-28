-- ========================================
-- V8__Create_Teacher_Schema.sql
-- ========================================
-- Based on: Quartz 2.3.2+ for PostgreSQL
-- ========================================

-- ===== Application Tables =====
CREATE TABLE IF NOT EXISTS teachers (
                                        id            BIGSERIAL PRIMARY KEY,
                                        user_id       BIGINT       NOT NULL UNIQUE,
                                        first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    gender        VARCHAR(20),                          -- male | female | other | prefer_not_to_say
    date_of_birth DATE,
    avatar_url    VARCHAR(255),
    location      VARCHAR(120),                         -- 'CS Cầu Giấy', 'Online'...
    bio           TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'pending', -- active | on_leave | pending
    rating        NUMERIC(2,1) NOT NULL DEFAULT 0,      -- 0.0 .. 5.0
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    version       BIGINT       DEFAULT 0,

    CONSTRAINT fk_teachers_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_teachers_status
    CHECK (status IN ('active', 'on_leave', 'pending')),
    CONSTRAINT chk_teachers_gender
    CHECK (gender IS NULL OR gender IN ('male', 'female', 'other', 'prefer_not_to_say')),
    CONSTRAINT chk_teachers_rating
    CHECK (rating >= 0 AND rating <= 5)
    );

CREATE INDEX IF NOT EXISTS idx_teachers_status ON teachers(status);
CREATE INDEX IF NOT EXISTS idx_teachers_user_id ON teachers(user_id);

-- 2. Bảng subjects — danh mục môn dạy (Scratch, Python, Robotics...)
CREATE TABLE IF NOT EXISTS subjects (
                                        id         BIGSERIAL    PRIMARY KEY,
                                        name       VARCHAR(50)  NOT NULL UNIQUE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version    BIGINT       DEFAULT 0
    );

-- 3. teacher_subjects — quan hệ M:N giữa teachers và subjects
CREATE TABLE IF NOT EXISTS teacher_subjects (
                                                teacher_id BIGINT  NOT NULL,
                                                subject_id BIGINT  NOT NULL,
                                                is_primary BOOLEAN NOT NULL DEFAULT FALSE,  -- primarySubject trong form GV

                                                PRIMARY KEY (teacher_id, subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_teacher_subjects_subject ON teacher_subjects(subject_id);

-- Đảm bảo mỗi teacher chỉ có 1 môn primary
CREATE UNIQUE INDEX IF NOT EXISTS uniq_teacher_primary_subject
    ON teacher_subjects(teacher_id)
    WHERE is_primary = TRUE;

-- 4. Seed một số môn cơ bản (idempotent)
INSERT INTO subjects (name) VALUES
                                ('Scratch'),
                                ('Python'),
                                ('JavaScript'),
                                ('Web Design'),
                                ('UI/UX'),
                                ('Robotics'),
                                ('Arduino'),
                                ('Game Dev'),
                                ('Unity'),
                                ('Math'),
                                ('English'),
                                ('AI Explorers')
    ON CONFLICT (name) DO NOTHING;
