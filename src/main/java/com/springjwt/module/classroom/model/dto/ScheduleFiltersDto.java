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
public class ScheduleFiltersDto {
    private List<ScheduleOptionDto> teachers;
    private List<ScheduleOptionDto> classes;
    private List<ScheduleOptionDto> locations;
}
