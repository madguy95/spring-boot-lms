package com.springjwt.module.audit.model.dto;

import com.springjwt.common.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDto {
    private Long id;
    private String correlationId;
    private String userId;          // ← THÊM FIELD NÀY
    private String username;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String description;
    private String requestUri;
    private String requestMethod;
    private String ipAddress;
    private String status;
    private Instant timestamp;
    private Long durationMs;
}
