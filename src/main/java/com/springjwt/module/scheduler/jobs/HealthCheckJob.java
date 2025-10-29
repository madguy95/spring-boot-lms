package com.springjwt.module.scheduler.jobs;

import com.springjwt.common.enums.ExecutionStatus;
import com.springjwt.core.redis.RedisService;
import com.springjwt.module.scheduler.business.JobHistoryService;
import com.springjwt.module.scheduler.domain.repository.ScheduledJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Health Check Job - Monitors system health (database, cache).
 * <p>
 * Production-ready implementation:
 * - Simple Job interface (no timeout handling needed)
 * - DisallowConcurrentExecution prevents overlap
 * - Cluster-safe (Quartz handles job distribution)
 * - Fast execution (< 5 seconds typically)
 * <p>
 * Timeout handling:
 * - Database queries have connection timeout (60s)
 * - Redis operations have timeout (60s from config)
 * - If job hangs, next instance will execute (cluster mode)
 * - APM tools (Datadog, New Relic) monitor execution time
 */
@Component
@Slf4j
@DisallowConcurrentExecution
public class HealthCheckJob implements Job {

    @Autowired
    private ScheduledJobRepository jobRepository;

    @Autowired(required = false)
    private RedisService redisService;

    @Autowired
    private JobHistoryService jobHistoryService;

    @Override
    public void execute(JobExecutionContext context) {
        long startTime = System.currentTimeMillis();
        Long jobId = context.getMergedJobDataMap().getLong("jobId");

        try {
            // Set MDC for logging
            MDC.put("jobId", String.valueOf(jobId));
            MDC.put("jobName", "HealthCheckJob");
            MDC.put("instanceId", context.getScheduler().getSchedulerInstanceId());

            log.info("Starting health check job, jobId: {}", jobId);

            // Perform basic health checks
            boolean databaseHealthy = checkDatabase();
            boolean cacheHealthy = checkCache();

            String result = String.format(
                    "Health check completed - Database: %s, Cache: %s",
                    databaseHealthy ? "UP" : "DOWN",
                    cacheHealthy ? "UP" : "DOWN"
            );

            ExecutionStatus status = (databaseHealthy && cacheHealthy)
                    ? ExecutionStatus.SUCCESS
                    : ExecutionStatus.FAILED;

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status", status.name());
            MDC.put("duration", String.valueOf(duration));

            jobHistoryService.saveExecutionHistory(jobId, status,
                    duration, result, null);

            log.info("Health check job completed in {}ms: {}", duration, result);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status", "FAILED");
            MDC.put("duration", String.valueOf(duration));

            log.error("Error in health check job after {}ms", duration, e);
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

    private boolean checkDatabase() {
        try {
            // Simple query to check database connectivity
            jobRepository.count();
            return true;
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }

    private boolean checkCache() {
        // Skip cache check if Redis is not available
        if (redisService == null) {
            log.debug("RedisService not available. Skipping cache health check.");
            return true; // Return true to not fail health check when Redis is disabled
        }

        try {
            // Perform a ping-like operation by setting and getting a test key
            String testKey = "health:check:" + UUID.randomUUID();
            String testValue = "OK";

            // Try to set a value with 10 seconds TTL
            redisService.setValue(testKey, testValue, TimeUnit.SECONDS, 10);

            // Try to get the value back
            Object retrievedValue = redisService.getValue(testKey);

            // Clean up test key
            redisService.removeKey(testKey);

            // Check if the value matches
            boolean isHealthy = testValue.equals(retrievedValue);

            if (isHealthy) {
                log.debug("Redis cache health check passed");
            } else {
                log.warn("Redis cache health check failed: value mismatch");
            }

            return isHealthy;
        } catch (Exception e) {
            log.error("Cache health check failed", e);
            return false;
        }
    }
}
