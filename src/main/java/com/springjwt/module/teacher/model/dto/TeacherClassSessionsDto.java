package com.springjwt.module.teacher.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherClassSessionsDto {
    private List<TeacherClassSessionDto> sessions;
    private String currentSessionId;
}
