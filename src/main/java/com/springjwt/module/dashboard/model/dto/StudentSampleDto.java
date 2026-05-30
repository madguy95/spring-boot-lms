package com.springjwt.module.dashboard.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentSampleDto {
    private String initials;
    /** Deterministic palette index; FE maps to a tailwind tone. */
    private Integer toneSeed;
}
