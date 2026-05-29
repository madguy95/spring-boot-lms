package com.springjwt.module.enrollment.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentClassDto {
    private Long id;
    private String name;
    private String label;
    private String schedule;
    private Integer enrolled;
    private Integer capacity;
}
