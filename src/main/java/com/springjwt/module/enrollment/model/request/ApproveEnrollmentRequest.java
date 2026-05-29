package com.springjwt.module.enrollment.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveEnrollmentRequest {

    @NotNull(message = "enrollment.validation.classId.required")
    private Long classId;
}
