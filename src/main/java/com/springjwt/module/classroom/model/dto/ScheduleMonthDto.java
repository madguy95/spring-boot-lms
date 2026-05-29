package com.springjwt.module.classroom.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleMonthDto {
    private ScheduleSummaryDto summary;
    private List<String> weekdayHeaders;
    private List<ScheduleMonthWeekDto> weeks;
}
