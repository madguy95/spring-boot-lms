package com.springjwt.module.teacher.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyClassesStatsDto {
    private MyClassesClassesStatsDto classes;
    private MyClassesStudentsStatsDto students;
}
