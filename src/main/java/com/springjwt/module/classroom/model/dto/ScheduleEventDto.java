package com.springjwt.module.classroom.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleEventDto {
    private String id;
    private String classId;
    private String teacherId;
    /** 0 = Mon … 6 = Sun. */
    private int dayIndex;
    /** Minutes from the FE grid's 08:00 baseline. */
    private int startOffsetMin;
    private int durationMin;
    private String title;
    private String detail;
    private String timeLabel;
    private String location;
    /** scratch | python | web | robotics | game_ai */
    private String category;
}
