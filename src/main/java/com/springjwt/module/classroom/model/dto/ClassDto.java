package com.springjwt.module.classroom.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassDto {
    private Long id;
    private String name;
    private String label;
    private String location;
    private String room;
    private String schedule;
    private Integer enrolled;
    private Integer capacity;
    // Admin-controlled stored status: draft | published | unpublished | cancelled.
    private String lifecycleStatus;
    // Derived status the UI renders / filters on:
    // draft | open | full | ongoing | completed | unpublished | cancelled.
    private String status;
    private String visibility;
    private String cancellationReason;
    private Integer totalSessions;
    private LocalDate startDate;
    private LocalDate endDate;
    private ClassCourseDto course;
    private ClassTeacherDto teacher;
    private List<ClassDayScheduleDto> daySchedules;
    private Instant createdAt;
    private Instant updatedAt;
}
