-- ========================================
-- V3__Create_Audit_Schema.sql
-- ========================================
-- Description: Audit trail schema for tracking system activities
-- Author: System
-- Date: 2025-10-29
-- ========================================

-- Table: audit_logs
-- Stores audit trail for all important actions in the system
-- NOTE: This is an append-only table (no UPDATE/DELETE operations)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,

    -- Correlation & Tracking
    correlation_id VARCHAR(100),

    -- User Information
    user_id VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL,

    -- Action Information
    action VARCHAR(20) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    description VARCHAR(500),

    -- Request Information
    request_uri VARCHAR(500),
    request_method VARCHAR(10),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),

    -- Execution Information
    status VARCHAR(20),
    error_message VARCHAR(1000),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT
);

COMMENT ON TABLE audit_logs IS 'Audit trail for system activities';

CREATE INDEX IF NOT EXISTS idx_audit_username ON audit_logs (username);
CREATE INDEX IF NOT EXISTS idx_audit_user_id ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs (timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_status ON audit_logs (status);
CREATE INDEX IF NOT EXISTS idx_audit_correlation ON audit_logs (correlation_id);

