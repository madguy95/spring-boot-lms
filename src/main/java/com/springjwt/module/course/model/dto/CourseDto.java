package com.springjwt.module.course.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDto {
    private Long id;
    private String code;
    private String title;
    private String tagline;
    private String description;
    private String tool;
    private String status;
    private Integer minAge;
    private Integer maxAge;
    private Integer totalSessions;
    private Integer sessionDurationMinutes;
    private Integer perClassCapacity;
    private Integer capacity;       // mirrors perClassCapacity until class enrollment is modelled
    private Integer classes;        // placeholder 0 until classes module exists
    private Integer enrolled;       // placeholder 0 until enrolment module exists
    private BigDecimal tuitionAmount;
    private BigDecimal originalTuitionAmount;
    private String coverUrl;
    private String introVideoUrl;
    private String pricingNotes;
    private String version;
    // Derived from sessions[].title; capped client-side in mock to 8 with "… N more" suffix.
    // We return the raw titles list here and let the FE keep its existing display logic.
    private List<String> curriculum;
    private List<CourseSessionDto> sessions;
    private List<CourseDiscountDto> discounts;
    private Instant createdAt;
    private Instant updatedAt;
}
