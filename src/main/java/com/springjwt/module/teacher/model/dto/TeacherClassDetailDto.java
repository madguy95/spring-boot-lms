package com.springjwt.module.teacher.model.dto;

import lombok.*;

/** Header + overview stats for the Teacher "Class detail" screen. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherClassDetailDto {
    private String id;
    private String classLabel;
    private String courseCode;
    private String courseName;
    private String status;          // running | upcoming | ended
    private String title;
    private String location;
    private String schedule;
    private String termRange;
    private Integer sessionCurrent;
    private Integer sessionTotal;
    private Integer studentCount;
    private Integer presentLastWeek;
    private Integer absentLastWeek;
    private Integer attendanceRate;  // 0..100
    private Integer attendedCount;
    private Integer attendedTotal;
    private Integer needsReviewCount;
    private String needsReviewSession;
    private String color;            // emerald|sky|amber|violet|rose|slate
    private String currentSessionId; // session code (e.g. 'B6'), "" when none
    /** Cover image URL inherited from the course; null when the course has none. */
    private String coverUrl;
}
