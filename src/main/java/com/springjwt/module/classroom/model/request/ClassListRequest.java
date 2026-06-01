package com.springjwt.module.classroom.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassListRequest {

    @Builder.Default
    @Min(value = 1, message = "class.validation.page.min")
    private int page = 1;

    @Builder.Default
    @Min(value = 1, message = "class.validation.size.min")
    @Max(value = 100, message = "class.validation.size.max")
    private int size = 50;

    // Filter is on derived display status — what the UI tabs show.
    // (lifecycle filter would only make sense for an admin-only view; not needed yet.)
    @Pattern(regexp = "^(draft|open|full|ongoing|completed|unpublished|cancelled)$",
            message = "class.validation.status.invalid")
    private String status;

    private String search;

    private Long courseId;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "(?i)^(asc|desc)$", message = "class.validation.sortDirection.invalid")
    private String sortDirection = "desc";
}
