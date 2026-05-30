package com.springjwt.module.dashboard.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardKpisDto {
    private CoursesKpiDto courses;
    private ClassesKpiDto classes;
    private StudentsKpiDto students;
    private PendingKpiDto pending;
}
