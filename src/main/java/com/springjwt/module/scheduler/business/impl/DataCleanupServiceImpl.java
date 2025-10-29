package com.springjwt.module.scheduler.business.impl;

import com.springjwt.module.audit.domain.repository.AuditLogRepository;
import com.springjwt.module.scheduler.business.DataCleanupService;
import com.springjwt.module.scheduler.domain.repository.JobExecutionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCleanupServiceImpl implements DataCleanupService {

    private final JobExecutionHistoryRepository historyRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public int cleanupExecutionHistory(int daysToKeep) {
        log.info("Starting cleanup of execution history older than {} days", daysToKeep);

        Instant cutoffDate = Instant.now().minusSeconds(daysToKeep * 24L * 60 * 60);
        int deletedCount = historyRepository.deleteByExecutionTimeBefore(cutoffDate);

        log.info("Deleted {} execution history records", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    public String cleanupAllOldData(int daysToKeep) {
        log.info("Starting full data cleanup for data older than {} days", daysToKeep);

        StringBuilder result = new StringBuilder();
        int totalDeleted = 0;

        try {
            // 1. Cleanup execution history
            int historyDeleted = cleanupExecutionHistory(daysToKeep);
            result.append(String.format("Execution histories: %d, ", historyDeleted));
            totalDeleted += historyDeleted;
            // 2. Cleanup audit logs (keep audit logs longer - 2x retention period)
            LocalDateTime auditCutoffDate = LocalDateTime.now().minusDays(daysToKeep * 2L);
            int auditDeleted = auditLogRepository.deleteByTimestampBefore(auditCutoffDate);
            result.append(String.format("Audit logs: %d, ", auditDeleted));
            totalDeleted += auditDeleted;


            result.append(String.format("Total: %d records cleaned up", totalDeleted));

            log.info("Full data cleanup completed: {}", result);
            return result.toString();

        } catch (Exception e) {
            log.error("Error during data cleanup, rolling back transaction", e);
            throw e;
        }
    }
}
