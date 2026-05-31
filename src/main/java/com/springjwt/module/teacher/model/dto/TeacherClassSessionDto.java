package com.springjwt.module.teacher.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherClassSessionDto {
    private String id;        // session code (e.g. 'B6')
    private Integer index;
    private String dateLabel;
    private String title;
    private String status;    // reviewed|in_progress|taught|upcoming
    private Integer needsReviewCount;
}
