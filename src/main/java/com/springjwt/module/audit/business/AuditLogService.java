package com.springjwt.module.audit.business;

import com.springjwt.module.audit.domain.entity.AuditLog;
import com.springjwt.module.audit.domain.service.AuditLogDomainService;
import com.springjwt.module.audit.model.dto.AuditLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogDomainService auditLogDomainService;

    public Page<AuditLogDto> getUserAuditLogs(String username, Pageable pageable) {
        return auditLogDomainService.findByUsername(username, pageable)
                .map(this::mapToDto);
    }

    public Page<AuditLogDto> getAuditLogsByDateRange(Instant startTime, Instant endTime, Pageable pageable) {
        return auditLogDomainService.findByTimestampBetween(startTime, endTime, pageable)
                .map(this::mapToDto);
    }

    public List<AuditLogDto> getAuditLogsByCorrelationId(String correlationId) {
        return auditLogDomainService.findByCorrelationId(correlationId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AuditLogDto> getEntityHistory(String entityType, String entityId) {
        return auditLogDomainService.findEntityHistory(entityType, entityId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AuditLogDto> getUserRecentActivity(String username, int days) {
        Instant since = Instant.now().minusSeconds((long) days * 24 * 60 * 60);
        return auditLogDomainService.findRecentActivityByUser(username, since)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AuditLogDto mapToDto(AuditLog entity) {
        return AuditLogDto.builder()
                .id(entity.getId())
                .correlationId(entity.getCorrelationId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .description(entity.getDescription())
                .requestUri(entity.getRequestUri())
                .requestMethod(entity.getRequestMethod())
                .ipAddress(entity.getIpAddress())
                .status(entity.getStatus())
                .timestamp(entity.getTimestamp())
                .durationMs(entity.getDurationMs())
                .build();
    }
}

