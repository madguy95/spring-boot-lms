package com.springjwt.module.course.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCourseStatusRequest {

    @NotBlank(message = "course.validation.status.required")
    @Pattern(regexp = "^(published|draft|unpublished)$", message = "course.validation.status.invalid")
    private String status;
}
