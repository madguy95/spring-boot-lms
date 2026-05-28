package com.springjwt.module.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Lean public-facing course payload for the landing page. Contains only the fields
 * unauthenticated visitors should see — no internal status, no draft/unpublished metadata,
 * no sessions/discounts. Always represents a course with status="published".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicCourseDto {
    private Long id;
    private String code;
    private String title;
    private String tagline;
    private String description;
    private String category;
    private String level;
    private Integer minAge;
    private Integer maxAge;
    private Integer totalSessions;
    private Integer sessionDurationMinutes;
    private BigDecimal tuitionAmount;
    private BigDecimal originalTuitionAmount;
    private String coverUrl;
    private Instant createdAt;
}
