package com.springjwt.module.consultation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateConsultationStatusRequest {

    @NotBlank(message = "consultation.validation.status.required")
    @Pattern(regexp = "^(new|contacted|enrolled|not_interested)$",
             message = "consultation.validation.status.invalid")
    private String status;
}
