package com.springjwt.module.enrollment.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDto {
    private Long id;
    private String studentName;
    private String initials;
    private Integer studentAge;
    private Integer studentGrade;
    private String parentName;
    private String parentPhone;
    private String parentEmail;
    private String note;
    private String status;
    private String channel;
    private String rejectionReason;
    private BigDecimal paymentAmount;
    private String paymentStatus;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant waitlistedAt;
    private Instant rejectedAt;
    private EnrollmentCourseDto requestedCourse;
    private EnrollmentClassDto assignedClass;
    private Instant createdAt;
    private Instant updatedAt;
}
