package com.springjwt.module.teacher.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherScheduleEventDto {
    private String id;
    private Long classId;
    /** Short class label, e.g. "A01". */
    private String classLabel;
    private String courseCode;
    private String title;
    /** 0 = Monday … 6 = Sunday. */
    private int dayIndex;
    /** Absolute minutes from midnight (e.g. 18:00 -> 1080). */
    private int startMin;
    private int durationMin;
    private String location;

    @JsonProperty("isOnline")
    private boolean isOnline;

    private int studentCount;
    /** emerald | sky | amber | violet | rose | slate. */
    private String color;

    @JsonProperty("isMakeup")
    private boolean isMakeup;

    /** Initials of the makeup student — only set when isMakeup. */
    private String makeupStudent;
}
