package com.springjwt.module.classroom.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassCourseDto {
    private Long id;
    private String code;
    private String title;
    private Integer totalSessions;
}
