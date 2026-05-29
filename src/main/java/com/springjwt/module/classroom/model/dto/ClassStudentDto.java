package com.springjwt.module.classroom.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassStudentDto {
    // Enrolment id — the roster row maps 1:1 to an approved enrolment.
    private Long id;
    private Long classId;
    private String name;
    private String initials;
    private Integer grade;
    private Integer age;
    // Attendance + status are placeholders for now (the attendance module
    // hasn't landed). UI renders them as defaults until real data arrives.
    private Integer attendance;
    private String status;
}
