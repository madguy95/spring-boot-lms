package com.springjwt.module.course.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseStatsDto {
    private long total;
    private long published;
    private long drafts;
    private long unpublished;
}
