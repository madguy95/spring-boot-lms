package com.springjwt.module.course.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCategoryTabDto {
    private String value;   // "all" or one of the category codes
    private long count;
}
