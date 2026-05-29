package com.springjwt.module.enrollment.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentCourseDto {
    private Long id;
    private String code;
    private String title;
    private Integer totalSessions;
    private Integer minAge;
    private Integer maxAge;
}
