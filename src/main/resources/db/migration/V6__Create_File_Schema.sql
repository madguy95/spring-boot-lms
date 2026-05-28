-- ========================================
-- V6__Create_File_Schema.sql
-- ========================================
-- Description: File storage schema
-- Author: System
-- Date: 2026-05-27
-- ========================================

CREATE TABLE IF NOT EXISTS files (
    id            BIGSERIAL PRIMARY KEY,

    -- File metadata
    original_name VARCHAR(255)       NOT NULL,
    stored_name   VARCHAR(255)       NOT NULL UNIQUE,
    file_path     VARCHAR(1000)      NOT NULL,
    file_size     BIGINT             NOT NULL,
    content_type  VARCHAR(100),
    storage_type  VARCHAR(20)        NOT NULL,
    access_url    VARCHAR(2000),
    folder        VARCHAR(500),
    is_deleted    BOOLEAN            NOT NULL DEFAULT FALSE,

    -- BaseEntity auditing columns
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    version       BIGINT             DEFAULT 0
);

COMMENT ON TABLE files IS 'Uploaded file metadata';

CREATE INDEX IF NOT EXISTS idx_files_stored_name  ON files (stored_name);
CREATE INDEX IF NOT EXISTS idx_files_storage_type ON files (storage_type);
CREATE INDEX IF NOT EXISTS idx_files_is_deleted   ON files (is_deleted);
CREATE INDEX IF NOT EXISTS idx_files_folder       ON files (folder);
CREATE INDEX IF NOT EXISTS idx_files_created_at   ON files (created_at);


