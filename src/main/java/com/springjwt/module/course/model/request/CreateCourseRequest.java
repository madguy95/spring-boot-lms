package com.springjwt.module.course.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseRequest {

    @NotBlank(message = "course.validation.title.required")
    @Size(max = 200, message = "course.validation.title.size")
    private String title;

    @NotBlank(message = "course.validation.code.required")
    @Size(max = 50, message = "course.validation.code.size")
    private String code;

    private String description;

    @NotBlank(message = "course.validation.tool.required")
    @Size(max = 50, message = "course.validation.tool.size")
    private String tool;

    @NotNull(message = "course.validation.minAge.required")
    @Min(value = 0, message = "course.validation.minAge.min")
    private Integer minAge;

    @NotNull(message = "course.validation.maxAge.required")
    @Min(value = 0, message = "course.validation.maxAge.min")
    private Integer maxAge;

    @NotNull(message = "course.validation.totalSessions.required")
    @Min(value = 0, message = "course.validation.totalSessions.min")
    private Integer totalSessions;

    @NotNull(message = "course.validation.sessionDurationMinutes.required")
    @Min(value = 0, message = "course.validation.sessionDurationMinutes.min")
    private Integer sessionDurationMinutes;

    @NotNull(message = "course.validation.perClassCapacity.required")
    @Min(value = 0, message = "course.validation.perClassCapacity.min")
    private Integer perClassCapacity;

    // Tagline derives from the first non-blank tag if provided.
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Valid
    @NotNull(message = "course.validation.sessions.required")
    @Builder.Default
    private List<CourseSessionInput> sessions = new ArrayList<>();

    @NotNull(message = "course.validation.tuitionAmount.required")
    @DecimalMin(value = "0", message = "course.validation.tuitionAmount.min")
    private BigDecimal tuitionAmount;

    @Valid
    @Builder.Default
    private List<DiscountRuleInput> discounts = new ArrayList<>();

    private String pricingNotes;

    @Size(max = 500, message = "course.validation.coverUrl.size")
    private String coverUrl;

    @Size(max = 500, message = "course.validation.introVideoUrl.size")
    private String introVideoUrl;
}
