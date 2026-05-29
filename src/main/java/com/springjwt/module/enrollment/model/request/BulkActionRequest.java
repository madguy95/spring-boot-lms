package com.springjwt.module.enrollment.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkActionRequest {

    @NotEmpty(message = "enrollment.validation.bulk.ids.required")
    private List<Long> ids;

    // Reject is intentionally excluded from bulk — admin must give a reason per
    // row, so a one-button "reject all" would lose audit-quality detail.
    @Pattern(regexp = "^(approve|waitlist)$", message = "enrollment.validation.bulk.action.invalid")
    private String action;

    // Required only when action == 'approve'. Service validates this.
    private Long classId;

    @Size(max = 500, message = "enrollment.validation.reason.size")
    private String reason;
}
