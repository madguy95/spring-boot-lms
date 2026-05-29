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
public class ScheduleWeekDto {
    private ScheduleSummaryDto summary;
    private List<ScheduleDayDto> days;
    private List<ScheduleEventDto> events;
}
