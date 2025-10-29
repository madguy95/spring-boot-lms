package com.springjwt.module.scheduler.business;

import com.springjwt.common.enums.ExecutionStatus;

public interface JobHistoryService {

    /**
     * Save job execution history in a separate transaction
     * This ensures history is saved even if the job fails
     *
     * @param jobId Job ID
     * @param status Execution status
     * @param durationMs Duration in milliseconds
     * @param result Result message
     * @param errorMessage Error message if failed
     */
    void saveExecutionHistory(Long jobId, ExecutionStatus status,
                              long durationMs, String result, String errorMessage);
}