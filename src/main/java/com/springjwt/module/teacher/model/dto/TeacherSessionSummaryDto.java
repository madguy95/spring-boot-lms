package com.springjwt.module.teacher.model.dto;

import lombok.*;

/** Whole-class per-session review summary. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSessionSummaryDto {
    private String comment;
    private String rating;   // weak|average|good|great|excellent
}
