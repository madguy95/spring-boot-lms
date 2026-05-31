package com.springjwt.module.teacher.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyClassesSummaryDto {
    private MyClassesStatsDto stats;
    private List<MyClassDto> classes;
}
