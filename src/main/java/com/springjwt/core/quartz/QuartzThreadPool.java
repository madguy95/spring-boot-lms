package com.springjwt.core.quartz;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom Quartz ThreadPool using Java 21 Virtual Threads
 *
 * Benefits:
 * - Lightweight: millions of virtual threads vs thousands of platform threads
 * - Efficient for I/O-bound jobs: database queries, API calls, cache operations
 * - No thread pool size limits needed
 * - Better resource utilization
 *
 * Use case: Perfect for jobs like DataCleanupJob, HealthCheckJob
 */
public class QuartzThreadPool implements ThreadPool {

    private static final Logger log = LoggerFactory.getLogger(QuartzThreadPool.class);

    private ExecutorService executor;
    private final AtomicInteger runningJobCount = new AtomicInteger(0);
    private final AtomicInteger totalExecutedJobs = new AtomicInteger(0);

    // Store instance information for clustering
    private String instanceId;
    private String instanceName;

    // Dummy property for compatibility with Quartz configuration
    // Virtual threads don't need this, but Quartz may try to set it
    @SuppressWarnings("unused")
    private int threadCount = -1;

    @Override
    public void initialize() throws SchedulerConfigException {
        log.info("Initializing Virtual Thread Pool for Quartz Scheduler");
        log.info("Instance ID: {}, Instance Name: {}", instanceId, instanceName);

        // Create virtual thread executor - no need to specify thread count!
        // Virtual threads are so lightweight that we can create millions of them
        executor = Executors.newVirtualThreadPerTaskExecutor();

        log.info("Virtual Thread Pool initialized successfully");
        log.info("Virtual threads are lightweight - no hard limit on concurrent jobs");
    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        log.info("Shutting down Virtual Thread Pool, waitForJobsToComplete: {}", waitForJobsToComplete);

        if (waitForJobsToComplete) {
            executor.shutdown();
            try {
                // Wait up to 60 seconds for jobs to complete
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Virtual Thread Pool did not terminate within 60 seconds, forcing shutdown");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for Virtual Thread Pool shutdown", e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } else {
            executor.shutdownNow();
        }

        log.info("Virtual Thread Pool shutdown complete. Total jobs executed: {}", totalExecutedJobs.get());
    }

    @Override
    public boolean runInThread(Runnable runnable) {
        if (executor.isShutdown()) {
            log.warn("Cannot run job - Virtual Thread Pool is shutdown");
            return false;
        }

        try {
            executor.execute(() -> {
                int currentCount = runningJobCount.incrementAndGet();
                int totalCount = totalExecutedJobs.incrementAndGet();

                log.debug("Job started on virtual thread. Current running: {}, Total executed: {}",
                         currentCount, totalCount);

                try {
                    runnable.run();
                } finally {
                    runningJobCount.decrementAndGet();
                }
            });
            return true;
        } catch (Exception e) {
            log.error("Failed to submit job to Virtual Thread Pool", e);
            return false;
        }
    }

    @Override
    public int blockForAvailableThreads() {
        // Virtual threads are so lightweight, always return a large number
        // This tells Quartz that we always have capacity
        int running = runningJobCount.get();
        // Limit 1000 concurrent jobs
        return Math.max(1, 1000 - running);
    }

    @Override
    public int getPoolSize() {
        // Virtual threads don't have a fixed pool size
        // Return current running jobs as approximation
        return runningJobCount.get();
    }

    @Override
    public void setInstanceId(String schedInstId) {
        this.instanceId = schedInstId;
        log.debug("ThreadPool instance ID set to: {}", schedInstId);
    }

    @Override
    public void setInstanceName(String schedName) {
        this.instanceName = schedName;
        log.debug("ThreadPool instance name set to: {}", schedName);
    }

    /**
     * Setter for threadCount (for Quartz compatibility)
     * Note: Virtual threads don't use this value, but Quartz may try to set it
     */
    public void setThreadCount(int count) {
        this.threadCount = count;
        log.debug("ThreadCount property set to: {} (ignored - using virtual threads)", count);
    }

    /**
     * Getter for threadCount (for Quartz compatibility)
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Get current number of running jobs
     */
    public int getRunningJobCount() {
        return runningJobCount.get();
    }

    /**
     * Get total number of executed jobs since startup
     */
    public int getTotalExecutedJobs() {
        return totalExecutedJobs.get();
    }
}
