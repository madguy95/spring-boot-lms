package com.springjwt.module.course.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseListRequest {

    @Builder.Default
    @Min(value = 1, message = "course.validation.page.min")
    private int page = 1;

    @Builder.Default
    @Min(value = 1, message = "course.validation.size.min")
    @Max(value = 100, message = "course.validation.size.max")
    private int size = 50;

    @Pattern(regexp = "^(coding|design|robotics|stem|language|game)$",
            message = "course.validation.category.invalid")
    private String category;

    @Pattern(regexp = "^(published|draft|unpublished)$",
            message = "course.validation.status.invalid")
    private String status;

    private String search;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "(?i)^(asc|desc)$", message = "course.validation.sortDirection.invalid")
    private String sortDirection = "desc";
}
