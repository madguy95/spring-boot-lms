package com.springjwt.module.course.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCourseRequest {

    // All fields optional — partial update. Null means "leave as-is".
    @Size(max = 200, message = "course.validation.title.size")
    private String title;

    @Size(max = 50, message = "course.validation.code.size")
    private String code;

    private String description;

    @Pattern(regexp = "^(coding|design|robotics|stem|language|game)$",
            message = "course.validation.category.invalid")
    private String category;

    @Pattern(regexp = "^(beginner|intermediate|advanced)$",
            message = "course.validation.level.invalid")
    private String level;

    @Pattern(regexp = "^(published|draft|unpublished)$",
            message = "course.validation.status.invalid")
    private String status;

    @Min(value = 0, message = "course.validation.minAge.min")
    private Integer minAge;

    @Min(value = 0, message = "course.validation.maxAge.min")
    private Integer maxAge;

    @Min(value = 0, message = "course.validation.totalSessions.min")
    private Integer totalSessions;

    @Min(value = 0, message = "course.validation.sessionDurationMinutes.min")
    private Integer sessionDurationMinutes;

    @Min(value = 0, message = "course.validation.perClassCapacity.min")
    private Integer perClassCapacity;

    // Null = don't touch; empty list = clear.
    private List<String> tags;

    @Valid
    private List<CourseSessionInput> sessions;

    @DecimalMin(value = "0", message = "course.validation.tuitionAmount.min")
    private BigDecimal tuitionAmount;

    @Valid
    private List<DiscountRuleInput> discounts;

    private String pricingNotes;

    @Size(max = 500, message = "course.validation.coverUrl.size")
    private String coverUrl;

    @Size(max = 500, message = "course.validation.introVideoUrl.size")
    private String introVideoUrl;
}
