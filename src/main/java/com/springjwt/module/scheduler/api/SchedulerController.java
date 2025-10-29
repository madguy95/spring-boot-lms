package com.springjwt.module.scheduler.api;

import com.springjwt.common.base.request.BasePagingRequest;
import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.common.util.MessageUtil;
import com.springjwt.module.scheduler.business.SchedulerService;
import com.springjwt.module.scheduler.model.dto.JobExecutionDTO;
import com.springjwt.module.scheduler.model.dto.ScheduledJobDTO;
import com.springjwt.module.scheduler.model.request.CreateJobRequest;
import com.springjwt.module.scheduler.model.request.UpdateJobRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing scheduled jobs using Quartz Scheduler.
 * <p>
 * This controller provides endpoints for CRUD operations on scheduled jobs,
 * as well as job control operations (pause, resume, trigger).
 * <p>
 * All endpoints require ADMIN role for security.
 */
@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
@Tag(name = "Scheduler Management", description = "APIs for managing Quartz scheduled jobs")
@ConditionalOnProperty(name = "app.quartz.enabled", havingValue = "true", matchIfMissing = false)
public class SchedulerController {

    private final SchedulerService schedulerService;
    private final MessageUtil messageUtil;

    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new scheduled job",
            description = "Creates a new scheduled job and immediately schedules it in Quartz. " +
                    "The job will start executing according to the provided cron expression."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or job already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin role required")
    })
    public ResponseEntity<ApiResult<ScheduledJobDTO>> createJob(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job configuration including name, group, class, and cron expression",
                    required = true
            )
            CreateJobRequest request) {
        return ResponseFactory.success(schedulerService.createJob(request));
    }

    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update scheduled job",
            description = "Updates an existing job's configuration. " +
                    "If cron expression or parameters are changed, the job will be rescheduled in Quartz."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<ScheduledJobDTO>> updateJob(
            @Parameter(description = "Job ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated job configuration (all fields optional)",
                    required = true
            )
            UpdateJobRequest request) {
        return ResponseFactory.success(schedulerService.updateJob(id, request));
    }

    @DeleteMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete scheduled job",
            description = "Deletes a job and unschedules it from Quartz. " +
                    "All execution history for this job will also be deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<String>> deleteJob(
            @Parameter(description = "Job ID to delete", required = true, example = "1")
            @PathVariable Long id) {
        schedulerService.deleteJob(id);
        return ResponseFactory.success(messageUtil.getMessage("scheduler.job.deleted.success"));
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get job by ID",
            description = "Retrieves detailed information about a specific scheduled job"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job found"),
            @ApiResponse(responseCode = "400", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<ScheduledJobDTO>> getJobById(
            @Parameter(description = "Job ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseFactory.success(schedulerService.getJobById(id));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all scheduled jobs",
            description = "Retrieves all scheduled jobs with pagination support. " +
                    "Results are sorted by creation date in descending order."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<PagedResult<ScheduledJobDTO>>> getAllJobs(
            @Valid @Parameter(description = "Pagination parameters")
            BasePagingRequest request) {
        return ResponseFactory.success(schedulerService.getAllJobs(request));
    }

    @GetMapping("/jobs/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all active jobs",
            description = "Retrieves all jobs with ACTIVE status (not paused)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active jobs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<List<ScheduledJobDTO>>> getActiveJobs() {
        return ResponseFactory.success(schedulerService.getActiveJobs());
    }

    @PostMapping("/jobs/{id}/pause")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Pause job",
            description = "Pauses a scheduled job. The job will stop executing until resumed. " +
                    "The job status will be changed to PAUSED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job paused successfully"),
            @ApiResponse(responseCode = "400", description = "Job not found or already paused"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<String>> pauseJob(
            @Parameter(description = "Job ID to pause", required = true, example = "1")
            @PathVariable Long id) {
        schedulerService.pauseJob(id);
        return ResponseFactory.success(messageUtil.getMessage("scheduler.job.paused.success"));
    }

    @PostMapping("/jobs/{id}/resume")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Resume job",
            description = "Resumes a paused job. The job will start executing according to its cron schedule. " +
                    "The job status will be changed to ACTIVE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job resumed successfully"),
            @ApiResponse(responseCode = "400", description = "Job not found or not paused"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<String>> resumeJob(
            @Parameter(description = "Job ID to resume", required = true, example = "1")
            @PathVariable Long id) {
        schedulerService.resumeJob(id);
        return ResponseFactory.success(messageUtil.getMessage("scheduler.job.resumed.success"));
    }

    @PostMapping("/jobs/{id}/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Trigger job immediately",
            description = "Manually triggers a job to run immediately, outside of its regular schedule. " +
                    "This does not affect the regular schedule."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job triggered successfully"),
            @ApiResponse(responseCode = "400", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<String>> triggerJob(
            @Parameter(description = "Job ID to trigger", required = true, example = "1")
            @PathVariable Long id) {
        schedulerService.triggerJob(id);
        return ResponseFactory.success(messageUtil.getMessage("scheduler.job.triggered.success"));
    }

    @GetMapping("/jobs/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get job execution history",
            description = "Retrieves execution history for a specific job with pagination. " +
                    "Shows execution time, status, duration, results, and errors."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execution history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResult<PagedResult<JobExecutionDTO>>> getJobExecutionHistory(
            @Parameter(description = "Job ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @Parameter(description = "Pagination parameters")
            BasePagingRequest request) {
        return ResponseFactory.success(schedulerService.getJobExecutionHistory(id, request));
    }
}

