package com.springjwt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExecutionStatus {
    SUCCESS("Success", "Job executed successfully"),
    FAILED("Failed", "Job execution failed"),
    RUNNING("Running", "Job is currently running");

    private final String displayName;
    private final String description;
}

