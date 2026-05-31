package com.springjwt.module.teacher.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherScheduleClassChipDto {
    private Long id;
    /** Short class label, e.g. "A01". */
    private String label;
    /** emerald | sky | amber | violet | rose | slate. */
    private String color;
}
