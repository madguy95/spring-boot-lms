package com.springjwt.module.teacher.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyClassesStudentsStatsDto {
    /** Total students across all the teacher's classes (sum of enrolment). */
    private long total;
}
