package com.springjwt.module.classroom.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleMonthCellDto {
    private LocalDate isoDate;
    private int date;
    /** False when the cell belongs to the leading/trailing days of an adjacent month. */
    private boolean inMonth;
    private Boolean isToday;
    private int eventCount;
    /** Capped slice of events for preview — full list is reachable via day view. */
    private List<ScheduleEventDto> events;
}
