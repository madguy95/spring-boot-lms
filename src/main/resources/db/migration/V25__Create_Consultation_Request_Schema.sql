-- ========================================
-- V25__Create_Consultation_Request_Schema.sql
-- ========================================
-- Stores consultation/advice requests submitted by parents via the public
-- website form. Admin reviews each row from the "Consultation Requests" page,
-- contacts the parent, and moves the row through the status pipeline:
--   new → contacted → enrolled | not_interested
-- ========================================

CREATE TABLE IF NOT EXISTS consultation_requests (
    id                      BIGSERIAL    PRIMARY KEY,

    parent_name             VARCHAR(120) NOT NULL,
    parent_phone            VARCHAR(20)  NOT NULL,
    child_name              VARCHAR(120),

    -- Nullable: parent may not have a specific course in mind yet.
    interested_course_id    BIGINT,

    note                    TEXT,

    status                  VARCHAR(30)  NOT NULL DEFAULT 'new',

    created_at              TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(50),
    updated_by              VARCHAR(50),
    version                 BIGINT       DEFAULT 0,

    CONSTRAINT fk_consultation_course
        FOREIGN KEY (interested_course_id) REFERENCES courses(id) ON DELETE SET NULL,
    CONSTRAINT chk_consultation_status
        CHECK (status IN ('new', 'contacted', 'enrolled', 'not_interested'))
);

CREATE INDEX IF NOT EXISTS idx_consultation_status      ON consultation_requests(status);
CREATE INDEX IF NOT EXISTS idx_consultation_created_at  ON consultation_requests(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_consultation_course_id   ON consultation_requests(interested_course_id);
