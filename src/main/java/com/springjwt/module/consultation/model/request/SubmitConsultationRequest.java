package com.springjwt.module.consultation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitConsultationRequest {

    @NotBlank(message = "consultation.validation.parentName.required")
    @Size(max = 120, message = "consultation.validation.parentName.max")
    private String parentName;

    @NotBlank(message = "consultation.validation.parentPhone.required")
    @Size(max = 20, message = "consultation.validation.parentPhone.max")
    private String parentPhone;

    @Size(max = 120, message = "consultation.validation.childName.max")
    private String childName;

    private Long interestedCourseId;

    private String note;
}
