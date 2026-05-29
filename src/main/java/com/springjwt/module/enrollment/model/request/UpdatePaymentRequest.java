package com.springjwt.module.enrollment.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentRequest {

    @NotBlank(message = "enrollment.validation.paymentStatus.required")
    @Pattern(regexp = "^(unpaid|paid|partial)$", message = "enrollment.validation.paymentStatus.invalid")
    private String status;

    // Optional — admin can record the amount alongside the status flip. When
    // omitted we leave the existing amount on the entity untouched.
    @DecimalMin(value = "0.0", inclusive = true, message = "enrollment.validation.paymentAmount.min")
    private BigDecimal amount;
}
