package com.springjwt.module.dashboard.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassesKpiDto {
    private long running;
    private long delta;
    private long offline;
    private long online;
}
