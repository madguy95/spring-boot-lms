package com.springjwt.module.scheduler.model.dto;

import com.springjwt.common.enums.ExecutionStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionDTO {

    private Long id;
    private Long jobId;
    private String jobName;
    private Instant executionTime;
    private ExecutionStatus status;
    private Long durationMs;
    private String result;
    private String errorMessage;
}

