package com.springjwt.module.teacher.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyClassStudentPreviewDto {
    private String initials;
    /** Stable seed so the FE picks a consistent avatar tone for this student. */
    private Integer toneSeed;
}
