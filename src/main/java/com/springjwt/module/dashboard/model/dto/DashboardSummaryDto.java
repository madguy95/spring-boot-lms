package com.springjwt.module.dashboard.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {
    private DashboardHeaderDto header;
    private DashboardKpisDto kpis;
    private List<DashboardRecentEnrollmentDto> recentEnrollments;
    private List<DashboardUpcomingClassDto> upcomingClasses;
    private List<DashboardCourseFillDto> courseFill;
}
