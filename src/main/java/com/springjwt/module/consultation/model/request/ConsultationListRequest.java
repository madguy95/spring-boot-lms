package com.springjwt.module.consultation.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationListRequest {

    @Builder.Default
    @Min(value = 1, message = "consultation.validation.page.min")
    private int page = 1;

    @Builder.Default
    @Min(value = 1, message = "consultation.validation.size.min")
    @Max(value = 100, message = "consultation.validation.size.max")
    private int size = 20;

    @Pattern(regexp = "^(new|contacted|enrolled|not_interested)$",
             message = "consultation.validation.status.invalid")
    private String status;

    private String search;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "(?i)^(asc|desc)$", message = "consultation.validation.sortDirection.invalid")
    private String sortDirection = "desc";
}
