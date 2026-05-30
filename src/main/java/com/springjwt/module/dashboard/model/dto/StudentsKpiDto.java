package com.springjwt.module.dashboard.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentsKpiDto {
    private long enrolled;
    private long delta;
    /** Up to 5 recent students for the avatar stack. */
    private List<StudentSampleDto> sample;
}
