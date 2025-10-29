package com.springjwt.module.audit.domain.service;

import com.springjwt.module.audit.domain.entity.AuditLog;
import com.springjwt.module.audit.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogDomainService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Save audit log in separate transaction to ensure it's saved even if main transaction fails
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog save(AuditLog auditLog) {
        try {
            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findByTimestampBetween(Instant startTime, Instant endTime, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findByCorrelationId(String correlationId) {
        return auditLogRepository.findByCorrelationId(correlationId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findEntityHistory(String entityType, String entityId) {
        return auditLogRepository.findEntityHistory(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findRecentActivityByUser(String username, Instant since) {
        return auditLogRepository.findRecentActivityByUser(username, since);
    }
}

