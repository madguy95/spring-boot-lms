package com.springjwt.module.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Public-facing detailed payload for a single course on the marketing site.
 *
 * <p>Superset of {@link PublicCourseDto}: also includes the full {@code description},
 * {@code introVideoUrl}, {@code perClassCapacity}, {@code pricingNotes} plus the ordered
 * list of {@code sessions} so the detail sheet can render a real syllabus.
 *
 * <p>Returned only for courses with {@code status="published"} — callers don't see drafts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicCourseDetailDto {
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
    private Integer perClassCapacity;
    private BigDecimal tuitionAmount;
    private BigDecimal originalTuitionAmount;
    private String coverUrl;
    private String introVideoUrl;
    private String pricingNotes;
    private List<CourseSessionDto> sessions;
    private Instant createdAt;
}
