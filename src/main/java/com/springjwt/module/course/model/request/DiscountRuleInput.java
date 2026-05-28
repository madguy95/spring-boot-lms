package com.springjwt.module.course.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountRuleInput {

    @NotBlank(message = "course.validation.discount.name.required")
    @Size(max = 120, message = "course.validation.discount.name.size")
    private String name;

    @NotBlank(message = "course.validation.discount.type.required")
    @Pattern(regexp = "^(percentage|fixed|special)$",
            message = "course.validation.discount.type.invalid")
    private String type;

    // For percentage/fixed: numeric value (sent as number).
    // For special: free-form text. Frontend keeps both in one `value` field.
    private Object value;

    @Pattern(regexp = "^(none|before_date|has_sibling|trial_only)$",
            message = "course.validation.discount.condition.invalid")
    @Builder.Default
    private String condition = "none";

    private LocalDate conditionDate;
}
