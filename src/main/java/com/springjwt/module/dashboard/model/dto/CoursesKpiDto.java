package com.springjwt.module.dashboard.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursesKpiDto {
    private long active;
    private long delta;
    /** 7 normalized values in [0..1] — Mon..Sun activity for the spark bars. */
    private List<Double> weeklyTrend;
}
