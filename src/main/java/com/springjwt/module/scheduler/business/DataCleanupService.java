package com.springjwt.module.scheduler.business;

public interface DataCleanupService {

    /**
     * Cleanup old execution histories
     * @param daysToKeep Number of days to keep history
     * @return Number of records deleted
     */
    int cleanupExecutionHistory(int daysToKeep);

    /**
     * Cleanup all old data (execution history, audit logs, etc.)
     * @param daysToKeep Number of days to keep data
     * @return Summary of cleanup result
     */
    String cleanupAllOldData(int daysToKeep);
}

