package com.springjwt.module.dashboard.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardRecentEnrollmentDto {
    private Long id;
    private String studentName;
    private String initials;
    private Integer studentGrade;
    private Integer studentAge;
    private String courseTitle;
    /** parent_app | website | referral | website_workshop */
    private String channel;
    private Instant submittedAt;
    /** pending | active | waitlist | rejected */
    private String status;
}
