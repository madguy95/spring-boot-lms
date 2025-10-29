package com.springjwt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobStatus {
    ACTIVE("Active", "Job is active and running"),
    PAUSED("Paused", "Job is temporarily paused"),
    DISABLED("Disabled", "Job is disabled");

    private final String displayName;
    private final String description;
}

