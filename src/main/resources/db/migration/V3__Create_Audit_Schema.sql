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
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

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
    timestamp TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    duration_ms BIGINT,

    -- Indexes for common queries
    INDEX idx_audit_username (username),
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_status (status),
    INDEX idx_audit_correlation (correlation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for system activities';

