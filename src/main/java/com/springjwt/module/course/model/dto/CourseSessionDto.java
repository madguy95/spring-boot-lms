package com.springjwt.module.course.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSessionDto {
    private String title;
    private String description;
}
