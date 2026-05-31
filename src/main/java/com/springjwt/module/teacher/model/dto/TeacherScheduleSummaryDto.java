package com.springjwt.module.teacher.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherScheduleSummaryDto {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    /** Set only when today falls inside this week — drives the "now" line + subtitle. */
    private LocalDate todayIso;
    /** Sessions the teacher runs this week. */
    private int sessionsThisWeek;
    /** Total teaching minutes across the week (FE renders as hours). */
    private int teachingMinutes;
    /** How many of the week's sessions are delivered online. */
    private int onlineCount;
    /** Makeup sessions outside the regular cadence. */
    private int makeupCount;
}
