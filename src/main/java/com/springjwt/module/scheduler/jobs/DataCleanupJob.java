package com.springjwt.module.scheduler.jobs;

import com.springjwt.common.enums.ExecutionStatus;
import com.springjwt.module.scheduler.business.DataCleanupService;
import com.springjwt.module.scheduler.business.JobHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Data Cleanup Job - Cleans up old execution history and audit logs.
 * <p>
 * Production-ready implementation:
 * - Simple Job interface (no timeout handling needed)
 * - DisallowConcurrentExecution prevents overlap
 * - Transactional operations with built-in timeouts
 * - Cluster-safe execution
 * <p>
 * Timeout handling:
 * - Database operations have transaction timeout
 * - DELETE queries typically fast with proper indexes
 * - Batch processing in service layer
 * - If job hangs, next scheduled run will execute
 * - Production monitoring alerts on long-running jobs
 */
@Component
@Slf4j
@DisallowConcurrentExecution
public class DataCleanupJob implements Job {

    @Autowired
    private DataCleanupService dataCleanupService;

    @Autowired
    private JobHistoryService jobHistoryService;

    @Override
    public void execute(JobExecutionContext context) {
        long startTime = System.currentTimeMillis();
        Long jobId = context.getMergedJobDataMap().getLong("jobId");

        try {
            // Set MDC for logging
            MDC.put("jobId", String.valueOf(jobId));
            MDC.put("jobName", "DataCleanupJob");
            MDC.put("instanceId", context.getScheduler().getSchedulerInstanceId());

            log.info("Starting data cleanup job, jobId: {}", jobId);

            // Get days to keep from job parameters, default to 90 days
            Integer daysToKeep = context.getMergedJobDataMap().getIntValue("daysToKeep");
            if (daysToKeep == null || daysToKeep <= 0) {
                daysToKeep = 90;
            }

            log.info("Cleaning up data older than {} days", daysToKeep);

            // Execute cleanup in transactional service
            String result = dataCleanupService.cleanupAllOldData(daysToKeep);

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status", "SUCCESS");
            MDC.put("duration", String.valueOf(duration));

            jobHistoryService.saveExecutionHistory(jobId, ExecutionStatus.SUCCESS,
                    duration, result, null);

            log.info("Data cleanup job completed in {}ms: {}", duration, result);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status", "FAILED");
            MDC.put("duration", String.valueOf(duration));

            log.error("Error in data cleanup job after {}ms", duration, e);
            jobHistoryService.saveExecutionHistory(jobId, ExecutionStatus.FAILED,
                    duration, null, e.getMessage());
        } finally {
            // Clean up MDC
            MDC.remove("jobId");
            MDC.remove("jobName");
            MDC.remove("status");
            MDC.remove("duration");
            MDC.remove("instanceId");
        }
    }
}
