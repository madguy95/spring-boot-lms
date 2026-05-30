package com.springjwt.module.dashboard.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardUpcomingClassDto {
    private Long id;
    private Long classId;
    /** "HH:mm" 24h. */
    private String startTime;
    private int durationMin;
    private String title;
    private String detail;
    /** scratch | python | web | robotics | game_ai */
    private String category;
    private UpcomingClassBadgeDto badge;
}
