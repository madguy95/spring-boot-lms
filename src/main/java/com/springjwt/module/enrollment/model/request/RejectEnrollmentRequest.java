package com.springjwt.module.enrollment.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectEnrollmentRequest {

    @NotBlank(message = "enrollment.validation.reason.required")
    @Size(max = 500, message = "enrollment.validation.reason.size")
    private String reason;
}
