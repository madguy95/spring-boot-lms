package com.springjwt.module.dashboard.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardHeaderDto {
    private String greetingName;
    /** "Summer Term 2026 · Week 4 of 12" — free text the FE renders as-is. */
    private String termLabel;
    /** "Today · Mon, May 23" — shown under the Upcoming classes header. */
    private String todayLabel;
}
