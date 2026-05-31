package com.springjwt.module.teacher.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherClassStudentsDto {
    private List<TeacherRosterStudentDto> students;
    private Integer total;     // after search filter
    private Integer totalAll;  // whole roster
}
