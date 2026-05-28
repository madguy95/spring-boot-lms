package com.springjwt.module.teacher.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSubjectDto {
    private Long id;
    private String name;
    private boolean isPrimary;
}

