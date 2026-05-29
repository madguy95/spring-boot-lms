-- ========================================
-- V18__Create_Enrollment_Schema.sql
-- ========================================
-- Enrollment module: applications submitted by parents (or created manually by
-- admin) requesting a course for a student. Admin reviews each row from the
-- admin "Enrollments" page and either approves + assigns the student to a class,
-- moves the row to waitlist, or rejects it (with a reason).
--
-- Student / parent fields are denormalized snapshots taken at submission time
-- because the parent/student modules do not exist yet. Once those modules land,
-- we can add nullable FK columns alongside the snapshots without breaking rows.
-- ========================================

CREATE TABLE IF NOT EXISTS enrollments (
    id                    BIGSERIAL    PRIMARY KEY,

    -- Snapshot of student + parent info submitted with the application.
    student_name          VARCHAR(120) NOT NULL,
    student_age           INTEGER,
    student_grade         INTEGER,
    parent_name           VARCHAR(120) NOT NULL,
    parent_phone          VARCHAR(20),
    parent_email          VARCHAR(120),

    requested_course_id   BIGINT       NOT NULL,
    -- Only populated after admin approves + assigns. Null while pending/waitlist/rejected.
    assigned_class_id     BIGINT,

    note                  TEXT,

    status                VARCHAR(20)  NOT NULL DEFAULT 'pending',
    channel               VARCHAR(20)  NOT NULL DEFAULT 'parent_app',

    rejection_reason      VARCHAR(500),
    payment_amount        NUMERIC(15,2),
    payment_status        VARCHAR(20),

    submitted_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at           TIMESTAMP,
    waitlisted_at         TIMESTAMP,
    rejected_at           TIMESTAMP,

    created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    version               BIGINT       DEFAULT 0,

    CONSTRAINT fk_enrollments_course
        FOREIGN KEY (requested_course_id) REFERENCES courses(id),
    CONSTRAINT fk_enrollments_class
        FOREIGN KEY (assigned_class_id)   REFERENCES classes(id),
    CONSTRAINT chk_enrollments_status
        CHECK (status IN ('pending', 'active', 'waitlist', 'rejected')),
    CONSTRAINT chk_enrollments_channel
        CHECK (channel IN ('parent_app', 'website', 'referral')),
    CONSTRAINT chk_enrollments_payment_status
        CHECK (payment_status IS NULL OR payment_status IN ('unpaid', 'paid', 'partial')),
    CONSTRAINT chk_enrollments_student_age
        CHECK (student_age IS NULL OR (student_age >= 0 AND student_age <= 30)),
    CONSTRAINT chk_enrollments_student_grade
        CHECK (student_grade IS NULL OR (student_grade >= 0 AND student_grade <= 20))
);

CREATE INDEX IF NOT EXISTS idx_enrollments_status         ON enrollments(status);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id      ON enrollments(requested_course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_class_id       ON enrollments(assigned_class_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_submitted_at   ON enrollments(submitted_at);
