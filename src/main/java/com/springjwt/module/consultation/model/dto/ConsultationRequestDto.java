package com.springjwt.module.consultation.model.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRequestDto {
    private Long id;
    private String parentName;
    private String parentPhone;
    private String childName;
    private Long interestedCourseId;
    private String interestedCourseTitle;
    private String note;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
