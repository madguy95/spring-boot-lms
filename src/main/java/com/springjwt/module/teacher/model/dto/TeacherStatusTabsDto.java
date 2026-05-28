package com.springjwt.module.teacher.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherStatusTabsDto {
    private long active;
    private long onLeave;
    private long pending;
    private long total;
}

