package com.springjwt.module.scheduler.business.impl;

import com.springjwt.common.base.request.BasePagingRequest;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.common.enums.JobStatus;
import com.springjwt.common.exception.AppException;
import com.springjwt.common.exception.BadRequestException;
import com.springjwt.common.util.MessageUtil;
import com.springjwt.module.scheduler.business.SchedulerService;
import com.springjwt.module.scheduler.domain.entity.JobExecutionHistory;
import com.springjwt.module.scheduler.domain.entity.ScheduledJob;
import com.springjwt.module.scheduler.domain.repository.JobExecutionHistoryRepository;
import com.springjwt.module.scheduler.domain.repository.ScheduledJobRepository;
import com.springjwt.module.scheduler.model.dto.JobExecutionDTO;
import com.springjwt.module.scheduler.model.dto.ScheduledJobDTO;
import com.springjwt.module.scheduler.model.request.CreateJobRequest;
import com.springjwt.module.scheduler.model.request.UpdateJobRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of SchedulerService for managing Quartz scheduled jobs.
 * <p>
 * This service provides CRUD operations for scheduled jobs and integrates with
 * Quartz Scheduler for job execution. It uses MessageUtil for internationalized
 * error messages and logging.
 * <p>
 * All job operations are transactional to ensure data consistency between
 * the application database and Quartz's internal storage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.quartz.enabled", havingValue = "true", matchIfMissing = false)
public class SchedulerServiceImpl implements SchedulerService {

    private final Scheduler scheduler;
    private final ScheduledJobRepository jobRepository;
    private final JobExecutionHistoryRepository historyRepository;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public ScheduledJobDTO createJob(CreateJobRequest request) {
        // Check if job already exists in database
        if (jobRepository.existsByJobNameAndJobGroup(request.getJobName(), request.getJobGroup())) {
            throw new BadRequestException(
                    messageUtil.getMessage("scheduler.job.already.exists",
                            request.getJobName(), request.getJobGroup())
            );
        }

        // Validate job class exists and implements Job interface
        validateJobClass(request.getJobClass());

        // Validate cron expression before saving
        validateCronExpression(request.getCronExpression());

        // Create job entity with ACTIVE status by default
        ScheduledJob job = ScheduledJob.builder()
                .jobName(request.getJobName())
                .jobGroup(request.getJobGroup())
                .jobClass(request.getJobClass())
                .description(request.getDescription())
                .cronExpression(request.getCronExpression())
                .parameters(request.getParameters())
                .status(JobStatus.ACTIVE)
                .build();

        job = jobRepository.save(job);
        scheduleJob(job);
        log.info("Job created successfully: {} in group {}", job.getJobName(), job.getJobGroup());
        return toDTO(job);
    }

    @Override
    @Transactional
    public ScheduledJobDTO updateJob(Long jobId, UpdateJobRequest request) {
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BadRequestException(
                        messageUtil.getMessage("scheduler.job.not.found")
                ));

        boolean needsReschedule = false;

        // Update description if provided
        if (request.getDescription() != null) {
            job.setDescription(request.getDescription());
        }

        // Update cron expression if provided and different
        if (request.getCronExpression() != null &&
                !request.getCronExpression().equals(job.getCronExpression())) {
            // Validate new cron expression before updating
            validateCronExpression(request.getCronExpression());

            job.setCronExpression(request.getCronExpression());
            needsReschedule = true;
        }

        // Update parameters if provided
        if (request.getParameters() != null) {
            job.setParameters(request.getParameters());
            needsReschedule = true;
        }

        job = jobRepository.save(job);

        // Reschedule if cron or parameters changed and job is active
        if (needsReschedule && job.getStatus() == JobStatus.ACTIVE) {
            rescheduleJob(job);
        }

        log.info("Job updated successfully: {}", job.getJobName());
        return toDTO(job);
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BadRequestException(
                        messageUtil.getMessage("scheduler.job.not.found")
                ));

        // Unschedule from Quartz before deleting from database
        unscheduleJob(job);
        jobRepository.delete(job);

        log.info("Job deleted successfully: {}", job.getJobName());
    }

    @Override
    public ScheduledJobDTO getJobById(Long jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BadRequestException(
                        messageUtil.getMessage("scheduler.job.not.found")
                ));
        return toDTO(job);
    }

    @Override
    public PagedResult<ScheduledJobDTO> getAllJobs(BasePagingRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ScheduledJob> page = jobRepository.findAll(pageable);

        List<ScheduledJobDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PagedResult.<ScheduledJobDTO>builder()
                .data(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void pauseJob(Long jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BadRequestException(
                        messageUtil.getMessage("scheduler.job.not.found")
                ));

        try {
            // Pause the job in Quartz
            scheduler.pauseJob(JobKey.jobKey(job.getJobName(), job.getJobGroup()));

            // Update status in database
            job.setStatus(JobStatus.PAUSED);
            jobRepository.save(job);

            log.info("Job paused successfully: {}", job.getJobName());

        } catch (SchedulerException e) {
            log.error("Failed to pause job: {}", job.getJobName(), e);
            throw new AppException(messageUtil.getMessage("scheduler.job.pause.failed"));
        }
    }

    @Override
    @Transactional
    public void resumeJob(Long jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BadRequestException(
                        messageUtil.getMessage("scheduler.job.not.found")
                ));

        try {
            // Resume the job in Quartz
            scheduler.resumeJob(JobKey.jobKey(job.getJobName(), job.getJobGroup()));

            // Update status in database
            job.setStatus(JobStatus.ACTIVE);
            jobRepository.save(job);

            log.info("Job resumed successfully: {}", job.getJobName());

        } catch (SchedulerException e) {
            log.error("Failed to resume job: {}", job.getJobName(), e);
            throw new AppException(messageUtil.getMessage("scheduler.job.resume.failed"));
        }
    }

    @Override
    public void triggerJob(Long jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BadRequestException(
                        messageUtil.getMessage("scheduler.job.not.found")
                ));

        try {
            // Manually trigger the job to run immediately
            scheduler.triggerJob(JobKey.jobKey(job.getJobName(), job.getJobGroup()));
            log.info("Job triggered successfully: {}", job.getJobName());

        } catch (SchedulerException e) {
            log.error("Failed to trigger job: {}", job.getJobName(), e);
            throw new AppException(messageUtil.getMessage("scheduler.job.trigger.failed"));
        }
    }

    @Override
    public void scheduleJob(ScheduledJob job) {
        try {
            // Build Quartz JobDetail and Trigger from our job entity
            JobDetail jobDetail = buildJobDetail(job);
            Trigger trigger = buildTrigger(job);

            // Schedule the job in Quartz
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Job scheduled in Quartz: {}", job.getJobName());

        } catch (SchedulerException e) {
            log.error("Error scheduling job in Quartz: {}", job.getJobName(), e);
            throw new AppException(messageUtil.getMessage("scheduler.job.schedule.failed"));
        }
    }

    @Override
    public void rescheduleJob(ScheduledJob job) {
        try {
            // Get the existing trigger key
            TriggerKey triggerKey = TriggerKey.triggerKey(
                    job.getJobName() + "_trigger",
                    job.getJobGroup()
            );

            // Build new trigger with updated cron expression
            Trigger newTrigger = buildTrigger(job);

            // Reschedule with the new trigger
            scheduler.rescheduleJob(triggerKey, newTrigger);

            log.info("Job rescheduled in Quartz: {}", job.getJobName());

        } catch (SchedulerException e) {
            log.error("Error rescheduling job in Quartz: {}", job.getJobName(), e);
            throw new AppException(messageUtil.getMessage("scheduler.job.reschedule.failed"));
        }
    }

    @Override
    public void unscheduleJob(ScheduledJob job) {
        try {
            // Delete the job from Quartz (also removes associated triggers)
            JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
            scheduler.deleteJob(jobKey);
            log.info("Job unscheduled from Quartz: {}", job.getJobName());

        } catch (SchedulerException e) {
            log.error("Error unscheduling job from Quartz: {}", job.getJobName(), e);
            throw new AppException(messageUtil.getMessage("scheduler.job.unschedule.failed"));
        }
    }

    @Override
    public PagedResult<JobExecutionDTO> getJobExecutionHistory(Long jobId, BasePagingRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "executionTime")
        );

        Page<JobExecutionHistory> page = historyRepository.findByJobId(jobId, pageable);

        List<JobExecutionDTO> content = page.getContent().stream()
                .map(this::toExecutionDTO)
                .collect(Collectors.toList());

        return PagedResult.<JobExecutionDTO>builder()
                .data(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public List<ScheduledJobDTO> getActiveJobs() {
        return jobRepository.findByStatus(JobStatus.ACTIVE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validates that the cron expression is valid and can be parsed by Quartz.
     *
     * @param cronExpression The cron expression to validate
     * @throws BadRequestException if cron expression is invalid
     */
    private void validateCronExpression(String cronExpression) {
        try {
            // Validate cron expression syntax
            CronExpression.validateExpression(cronExpression);
            // Additional check: try to create a CronScheduleBuilder to ensure it's usable
            CronScheduleBuilder.cronSchedule(cronExpression);
        } catch (java.text.ParseException e) {
            log.error("Invalid cron expression: {}", cronExpression, e);
            throw new BadRequestException(
                    messageUtil.getMessage("scheduler.cron.invalid") + ": " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("Error validating cron expression: {}", cronExpression, e);
            throw new BadRequestException(
                    messageUtil.getMessage("scheduler.cron.invalid")
            );
        }
    }

    /**
     * Builds Quartz JobDetail from our ScheduledJob entity.
     *
     * @param job The scheduled job entity
     * @return Quartz JobDetail configured for execution
     */
    private JobDetail buildJobDetail(ScheduledJob job) {
        Class<? extends Job> jobClass = getJobClass(job.getJobClass());

        // Create job data map with job ID and custom parameters
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobId", job.getId());
        if (job.getParameters() != null) {
            jobDataMap.putAll(job.getParameters());
        }

        return JobBuilder.newJob(jobClass)
                .withIdentity(job.getJobName(), job.getJobGroup())
                .withDescription(job.getDescription())
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    /**
     * Builds Quartz Trigger with cron schedule from our ScheduledJob entity.
     *
     * @param job The scheduled job entity
     * @return Quartz Trigger configured with cron expression
     */
    private Trigger buildTrigger(ScheduledJob job) {
        return TriggerBuilder.newTrigger()
                .forJob(job.getJobName(), job.getJobGroup())
                .withIdentity(job.getJobName() + "_trigger", job.getJobGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()).withMisfireHandlingInstructionDoNothing())
                .build();
    }

    /**
     * Loads and validates the job class.
     *
     * @param jobClassName Fully qualified class name
     * @return The job class
     * @throws BadRequestException if class not found or invalid
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            log.error("Job class not found: {}", jobClassName, e);
            throw new BadRequestException(
                    messageUtil.getMessage("scheduler.job.class.not.found", jobClassName)
            );
        }
    }

    /**
     * Validates that the job class exists and can be loaded.
     *
     * @param jobClassName Fully qualified class name to validate
     * @throws BadRequestException if validation fails
     */
    private void validateJobClass(String jobClassName) {
        getJobClass(jobClassName);
    }

    /**
     * Converts ScheduledJob entity to DTO for API response.
     *
     * @param job The scheduled job entity
     * @return ScheduledJobDTO for API response
     */
    private ScheduledJobDTO toDTO(ScheduledJob job) {
        return ScheduledJobDTO.builder()
                .id(job.getId())
                .jobName(job.getJobName())
                .jobGroup(job.getJobGroup())
                .jobClass(job.getJobClass())
                .description(job.getDescription())
                .cronExpression(job.getCronExpression())
                .status(job.getStatus())
                .parameters(job.getParameters())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .createdBy(job.getCreatedBy())
                .updatedBy(job.getUpdatedBy())
                .build();
    }

    /**
     * Converts JobExecutionHistory entity to DTO for API response.
     *
     * @param history The job execution history entity
     * @return JobExecutionDTO for API response
     */
    private JobExecutionDTO toExecutionDTO(JobExecutionHistory history) {
        return JobExecutionDTO.builder()
                .id(history.getId())
                .jobId(history.getJob().getId())
                .jobName(history.getJob().getJobName())
                .executionTime(history.getExecutionTime())
                .status(history.getStatus())
                .durationMs(history.getDurationMs())
                .result(history.getResult())
                .errorMessage(history.getErrorMessage())
                .build();
    }
}

