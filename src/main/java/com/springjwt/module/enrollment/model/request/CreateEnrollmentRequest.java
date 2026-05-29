package com.springjwt.module.enrollment.model.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEnrollmentRequest {

    @NotBlank(message = "enrollment.validation.studentName.required")
    @Size(max = 120, message = "enrollment.validation.studentName.size")
    private String studentName;

    @Min(value = 0, message = "enrollment.validation.studentAge.min")
    @Max(value = 30, message = "enrollment.validation.studentAge.max")
    private Integer studentAge;

    @Min(value = 0, message = "enrollment.validation.studentGrade.min")
    @Max(value = 20, message = "enrollment.validation.studentGrade.max")
    private Integer studentGrade;

    @NotBlank(message = "enrollment.validation.parentName.required")
    @Size(max = 120, message = "enrollment.validation.parentName.size")
    private String parentName;

    @Pattern(regexp = "^[0-9+\\-\\s]{8,20}$", message = "enrollment.validation.parentPhone.invalid")
    private String parentPhone;

    @Email(message = "enrollment.validation.parentEmail.invalid")
    @Size(max = 120, message = "enrollment.validation.parentEmail.size")
    private String parentEmail;

    @NotNull(message = "enrollment.validation.requestedCourse.required")
    private Long requestedCourseId;

    private String note;

    @Builder.Default
    @Pattern(regexp = "^(parent_app|website|referral)$", message = "enrollment.validation.channel.invalid")
    private String channel = "parent_app";

    @DecimalMin(value = "0.0", inclusive = true, message = "enrollment.validation.paymentAmount.min")
    private BigDecimal paymentAmount;

    @Pattern(regexp = "^(unpaid|paid|partial)$", message = "enrollment.validation.paymentStatus.invalid")
    private String paymentStatus;
}
