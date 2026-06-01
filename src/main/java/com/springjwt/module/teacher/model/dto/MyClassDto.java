package com.springjwt.module.teacher.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyClassDto {
    private Long id;
    /** Short class code, e.g. "A01". */
    private String code;
    /** Human level label derived from the course age band, e.g. "7–9 tuổi". */
    private String level;
    private String courseCode;
    /** Course title, e.g. "Scratch Cơ bản". */
    private String title;
    /** Class label, e.g. "Lớp A01". */
    private String classLabel;
    private String location;

    // Field-level @JsonProperty pins the JSON name to "isOnline"; without it
    // Jackson would strip the "is" prefix and emit "online", which the FE
    // (TeacherClassDto.isOnline) wouldn't read.
    @JsonProperty("isOnline")
    private boolean isOnline;

    private String schedule;
    private int studentCount;
    private Integer capacity;
    private int sessionCurrent;
    private int sessionTotal;
    /** running | upcoming | ended. */
    private String status;
    /** Sessions still awaiting review notes; null/0 when nothing pending. */
    private Integer needsReviewCount;
    /** dd/MM/yyyy — only set for ended classes. */
    private String endedAt;
    /** Days until start — only set for upcoming classes. */
    private Integer daysRemaining;
    private List<MyClassStudentPreviewDto> studentsPreview;
    /** Cover image URL inherited from the course; null when the course has none. */
    private String coverUrl;
}
