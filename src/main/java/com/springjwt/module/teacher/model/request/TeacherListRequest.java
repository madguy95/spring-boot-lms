package com.springjwt.module.teacher.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherListRequest {

    @Builder.Default
    @Min(value = 1, message = "teacher.validation.page.min")
    private int page = 1;

    @Builder.Default
    @Min(value = 1, message = "teacher.validation.size.min")
    @Max(value = 100, message = "teacher.validation.size.max")
    private int size = 10;

    @Pattern(regexp = "^(active|on_leave|pending)$", message = "teacher.validation.status.invalid")
    private String status;

    private String search;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "(?i)^(asc|desc)$", message = "teacher.validation.sortDirection.invalid")
    private String sortDirection = "desc";
}

