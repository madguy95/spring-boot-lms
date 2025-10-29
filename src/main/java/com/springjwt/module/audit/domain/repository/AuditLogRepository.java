package com.springjwt.module.audit.domain.repository;

import com.springjwt.common.enums.AuditAction;
import com.springjwt.module.audit.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(Instant startTime, Instant endTime, Pageable pageable);

    List<AuditLog> findByCorrelationId(String correlationId);

    @Query("SELECT a FROM AuditLog a WHERE a.username = :username AND a.timestamp >= :since")
    List<AuditLog> findRecentActivityByUser(String username, Instant since);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.timestamp DESC")
    List<AuditLog> findEntityHistory(String entityType, String entityId);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username AND a.action = :action AND a.timestamp >= :since")
    long countUserActionsSince(String username, AuditAction action, Instant since);

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    int deleteByTimestampBefore(LocalDateTime cutoffDate);
}

