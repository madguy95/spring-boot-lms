package com.springjwt.module.classroom.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayScheduleInput {

    @NotBlank(message = "class.validation.daySchedule.day.required")
    @Pattern(regexp = "^(Sun|Mon|Tue|Wed|Thu|Fri|Sat)$",
            message = "class.validation.daySchedule.day.invalid")
    private String day;

    @NotBlank(message = "class.validation.daySchedule.startTime.required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
            message = "class.validation.daySchedule.time.invalid")
    private String startTime;

    @NotBlank(message = "class.validation.daySchedule.endTime.required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
            message = "class.validation.daySchedule.time.invalid")
    private String endTime;
}
