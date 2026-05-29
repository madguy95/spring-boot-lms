package com.springjwt.module.enrollment.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentStatusTabsDto {
    private long all;
    private long pending;
    private long active;
    private long waitlist;
    private long rejected;
}
