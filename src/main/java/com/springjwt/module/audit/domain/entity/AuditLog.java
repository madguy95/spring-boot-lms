package com.springjwt.module.audit.domain.entity;

import com.springjwt.common.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity to store audit trail logs
 * Tracks who did what, when, and where
 * NOTE: Does NOT store old/new values for performance and security reasons
 * If you need entity details, query by entity_id from the actual entity table
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_username", columnList = "username"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Correlation ID from request for tracing
     */
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    /**
     * Username who performed the action
     */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    /**
     * Username who performed the action
     */
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /**
     * Action performed (CREATE, UPDATE, DELETE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    /**
     * Type of entity (User, File, Order, etc.)
     */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /**
     * ID of the entity being acted upon
     */
    @Column(name = "entity_id", length = 100)
    private String entityId;

    /**
     * Description of the action
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Request URI
     */
    @Column(name = "request_uri", length = 500)
    private String requestUri;

    /**
     * HTTP Method (GET, POST, PUT, DELETE)
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * Client IP address
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * User agent (browser info)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Status (SUCCESS, FAILED)
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * Error message if action failed
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Timestamp of the action
     */
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    /**
     * Execution duration in milliseconds
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }
}
