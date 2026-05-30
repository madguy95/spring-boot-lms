package com.springjwt.module.dashboard.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PendingKpiDto {
    private long count;
    /** Human-friendly average wait label (e.g. "1d 4h"). Null when count is 0. */
    private String avgWaitLabel;
}
