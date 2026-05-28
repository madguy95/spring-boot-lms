package com.springjwt.module.classroom.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassTeacherDto {
    private Long id;
    private String fullName;
    private String initials;
}
