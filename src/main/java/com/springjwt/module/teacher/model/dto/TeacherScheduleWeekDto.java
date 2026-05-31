package com.springjwt.module.teacher.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherScheduleWeekDto {
    private TeacherScheduleSummaryDto summary;
    private List<TeacherScheduleDayDto> days;
    private List<TeacherScheduleEventDto> events;
    private List<TeacherScheduleClassChipDto> classChips;
}
