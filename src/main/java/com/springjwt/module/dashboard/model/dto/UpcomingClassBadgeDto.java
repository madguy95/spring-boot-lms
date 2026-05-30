package com.springjwt.module.dashboard.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingClassBadgeDto {
    /** live | online | low_cap */
    private String kind;
    private String label;
}
