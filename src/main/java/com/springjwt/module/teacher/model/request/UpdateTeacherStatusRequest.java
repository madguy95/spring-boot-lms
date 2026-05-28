package com.springjwt.module.teacher.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTeacherStatusRequest {

    @NotBlank(message = "teacher.validation.status.required")
    @Pattern(regexp = "^(active|on_leave|pending)$", message = "teacher.validation.status.invalid")
    private String status;
}

