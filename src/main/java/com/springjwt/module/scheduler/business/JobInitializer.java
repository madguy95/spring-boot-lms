package com.springjwt.module.scheduler.business;

import com.springjwt.common.enums.JobStatus;
import com.springjwt.module.scheduler.domain.entity.ScheduledJob;
import com.springjwt.module.scheduler.domain.repository.ScheduledJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes and schedules all ACTIVE jobs from database when application starts.
 *
 * This component implements ApplicationRunner to execute after Spring context is fully loaded.
 * It acts as a bridge between the application's job metadata (scheduled_job table)
 * and Quartz's internal job storage (QRTZ_* tables).
 *
 * CLUSTER SAFE: Designed to work correctly when multiple application instances
 * start simultaneously. Uses checkExists() to prevent duplicate job scheduling
 * and handles race conditions gracefully.
 *
 * Key Features:
 * - Syncs jobs from scheduled_job table to Quartz scheduler
 * - Skips jobs that already exist (restored by Quartz or scheduled by another instance)
 * - Handles ObjectAlreadyExistsException for race conditions
 * - Provides detailed logging for monitoring and troubleshooting
 *
 * @see org.springframework.boot.ApplicationRunner
 * @see org.quartz.Scheduler
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.quartz.enabled", havingValue = "true")
public class JobInitializer implements ApplicationRunner {

    private final ScheduledJobRepository jobRepository;
    private final SchedulerService schedulerService;
    private final Scheduler scheduler;

    /**
     * Executes the job initialization process after application startup.
     *
     * This method:
     * 1. Waits for Quartz Scheduler to fully start
     * 2. Loads all ACTIVE jobs from the scheduled_job table
     * 3. Checks if each job already exists in Quartz (to avoid duplicates)
     * 4. Schedules new jobs that don't exist yet
     * 5. Handles race conditions when multiple instances start concurrently
     *
     * @param args Application startup arguments (not used)
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            // Wait for Quartz Scheduler to be fully initialized before proceeding
            // This ensures scheduler is ready to accept job scheduling requests
            if (!scheduler.isStarted()) {
                log.info("Waiting for Quartz Scheduler to start...");
                Thread.sleep(2000);
            }

            log.info("=================================================");
            log.info("Starting Job Initialization from Database");
            log.info("Scheduler Instance ID: {}", scheduler.getSchedulerInstanceId());
            log.info("=================================================");

            // Load all jobs with ACTIVE status from the database
            // These jobs should be scheduled in Quartz if not already present
            List<ScheduledJob> activeJobs = jobRepository.findByStatus(JobStatus.ACTIVE);

            if (activeJobs.isEmpty()) {
                log.info("No active jobs found in database");
                log.info("=================================================");
                return;
            }

            log.info("Found {} active job(s) to schedule", activeJobs.size());

            // Track statistics for final summary report
            int successCount = 0;
            int failCount = 0;
            int skippedCount = 0;

            // Iterate through each active job and attempt to schedule it in Quartz
            for (ScheduledJob job : activeJobs) {
                try {
                    // Create a unique identifier for the job using name and group
                    JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());

                    // CLUSTER SAFETY CHECK: Verify if job already exists in Quartz
                    // This prevents duplicate scheduling when:
                    // - Quartz auto-restored the job from QRTZ_* tables (overwriteExistingJobs=false)
                    // - Another instance already scheduled this job (clustering scenario)
                    if (scheduler.checkExists(jobKey)) {
                        log.info("Job already exists in Quartz (restored or scheduled by another instance): {}",
                                job.getJobName());
                        skippedCount++;
                        continue;
                    }

                    // Job doesn't exist yet, proceed to schedule it
                    log.info("Scheduling job: {} [{}] with cron: {}",
                            job.getJobName(),
                            job.getJobGroup(),
                            job.getCronExpression());

                    // Delegate to SchedulerService to build JobDetail and Trigger, then schedule
                    schedulerService.scheduleJob(job);
                    successCount++;

                    log.info("Successfully scheduled: {}", job.getJobName());

                } catch (org.quartz.ObjectAlreadyExistsException e) {
                    // RACE CONDITION HANDLING:
                    // This can occur when multiple instances check simultaneously:
                    // 1. Instance A checks: job doesn't exist
                    // 2. Instance B checks: job doesn't exist
                    // 3. Instance A schedules: SUCCESS
                    // 4. Instance B schedules: ObjectAlreadyExistsException
                    // We catch this exception and treat it as a skip (not an error)
                    log.info("Job already scheduled by another instance during race condition: {}",
                            job.getJobName());
                    skippedCount++;

                } catch (Exception e) {
                    // Catch any other unexpected exceptions during scheduling
                    // Log the error but continue processing remaining jobs
                    failCount++;
                    log.error("Failed to schedule job: {} - {}",
                            job.getJobName(),
                            e.getMessage(), e);
                }
            }

            // Print final summary of the initialization process
            log.info("=================================================");
            log.info("Job Initialization completed");
            log.info("Success: {}, Skipped: {}, Failed: {}, Total: {}",
                    successCount, skippedCount, failCount, activeJobs.size());
            log.info("=================================================");

        } catch (SchedulerException e) {
            // Failed to interact with Quartz Scheduler
            log.error("Failed to check scheduler status", e);

        } catch (InterruptedException e) {
            // Thread was interrupted while waiting for scheduler to start
            log.error("Interrupted while waiting for scheduler", e);
            Thread.currentThread().interrupt();

        } catch (Exception e) {
            // Catch-all for any other unexpected errors
            log.error("Unexpected error during job initialization", e);
        }
    }
}
