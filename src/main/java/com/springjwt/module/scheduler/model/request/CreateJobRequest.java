package com.springjwt.module.scheduler.model.request;

import com.springjwt.common.annotation.ValidCronExpression;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;

/**
 * Request object for creating a new scheduled job.
 * Contains job metadata and scheduling configuration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new scheduled job")
public class CreateJobRequest {

    @NotBlank(message = "{scheduler.job.name.required}")
    @Size(max = 100, message = "{scheduler.job.name.max}")
    @Schema(description = "Unique name of the job", example = "data-cleanup-job")
    private String jobName;

    @NotBlank(message = "{scheduler.job.group.required}")
    @Size(max = 100, message = "{scheduler.job.group.max}")
    @Schema(description = "Job group for organizing jobs", example = "maintenance")
    private String jobGroup;

    @NotBlank(message = "{scheduler.job.class.required}")
    @Schema(description = "Fully qualified class name of the job implementation",
            example = "com.springjwt.module.scheduler.jobs.DataCleanupJob")
    private String jobClass;

    @Schema(description = "Human-readable description of the job",
            example = "Cleans up old data records older than 90 days")
    private String description;

    @NotBlank(message = "{scheduler.cron.required}")
    @ValidCronExpression
    @Schema(description = "Cron expression for job scheduling. Format: second minute hour day month weekday",
            example = "0 0/10 * * * ?")
    private String cronExpression;

    @Schema(description = "Optional parameters passed to the job",
            example = "{\"daysToKeep\": \"90\", \"batchSize\": \"1000\"}")
    private Map<String, Object> parameters;
}
