package com.springjwt.module.classroom.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleSummaryDto {
    private String weekLabel;
    private String monthLabel;
    private String todayLabel;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private int totalSessions;
    private int totalTeachers;
    private int totalStudentHours;
}
