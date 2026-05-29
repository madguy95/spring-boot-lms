package com.springjwt.module.enrollment.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentListRequest {

    @Builder.Default
    @Min(value = 1, message = "enrollment.validation.page.min")
    private int page = 1;

    @Builder.Default
    @Min(value = 1, message = "enrollment.validation.size.min")
    @Max(value = 100, message = "enrollment.validation.size.max")
    private int size = 20;

    @Pattern(regexp = "^(pending|active|waitlist|rejected)$", message = "enrollment.validation.status.invalid")
    private String status;

    private String search;

    @Builder.Default
    private String sortBy = "submittedAt";

    @Builder.Default
    @Pattern(regexp = "(?i)^(asc|desc)$", message = "enrollment.validation.sortDirection.invalid")
    private String sortDirection = "desc";
}
