package com.springjwt.module.scheduler.model.dto;

import com.springjwt.common.enums.JobStatus;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledJobDTO {

    private Long id;
    private String jobName;
    private String jobGroup;
    private String jobClass;
    private String description;
    private String cronExpression;
    private JobStatus status;
    private Map<String, Object> parameters;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
