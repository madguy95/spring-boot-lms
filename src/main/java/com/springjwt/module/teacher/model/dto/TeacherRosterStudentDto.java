package com.springjwt.module.teacher.model.dto;

import lombok.*;

/** One roster row for the class-detail students tab. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRosterStudentDto {
    private String id;            // enrollment id as string
    private String name;
    private String initials;
    private Integer toneSeed;     // FE derives avatar tone from this
    private Integer age;
    private Integer grade;
    private String parentName;
    private String parentPhone;
    private Integer attendedSessions;
    private Integer totalSessions;
    private String lastStatus;    // present|excused|absent|makeup|unmarked
    private String lastSessionLabel;
}
