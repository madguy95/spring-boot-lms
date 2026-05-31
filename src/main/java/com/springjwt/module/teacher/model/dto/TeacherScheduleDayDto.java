package com.springjwt.module.teacher.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherScheduleDayDto {
    /** Day of month, e.g. 19. The FE derives the weekday label from the column index. */
    private int dayOfMonth;
    private Boolean isToday;
    private LocalDate isoDate;
}
