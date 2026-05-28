package com.springjwt.module.course.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseToolTabDto {
    private String value;   // "all" or one of the tool codes from master_data
    private long count;
}
