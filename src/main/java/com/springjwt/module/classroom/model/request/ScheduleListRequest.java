package com.springjwt.module.classroom.model.request;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleListRequest {

    // Anchor date interpreted per `view`:
    //   week  → snapped to the Monday of the ISO week containing the anchor
    //   day   → used as-is, payload covers just that date
    //   month → snapped to the 1st of the month containing the anchor
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate anchor;

    private Long teacherId;
    private Long classId;
    private String location;

    @Pattern(regexp = "^(week|day|month)$", message = "schedule.validation.view.invalid")
    @Builder.Default
    private String view = "week";
}
