package com.springjwt.module.teacher.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyClassesClassesStatsDto {
    /** Total classes the teacher owns (running + upcoming + ended). */
    private long total;
    private long running;
    private long upcoming;
    private long ended;
}
