package com.springjwt.module.teacher.model.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class TeacherScheduleRequest {

    /** Any day inside the target week; snapped to that week's Monday. Null = current week. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate anchor;

    /** Optional class filter (one of the chip ids). Null = all the teacher's classes. */
    private Long classId;
}
