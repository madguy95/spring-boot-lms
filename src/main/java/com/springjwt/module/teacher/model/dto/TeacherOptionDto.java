package com.springjwt.module.teacher.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherOptionDto {
    private Long id;
    private String fullName;
    private String email;
}

