package com.springjwt.module.classroom.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Body for PATCH /api/classes/{id}/lifecycle.
 *
 * `action`: publish | unpublish | cancel
 * `reason`: required when action = cancel (audit trail), ignored otherwise.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifecycleActionRequest {

    @NotBlank(message = "class.lifecycle.action.required")
    @Pattern(regexp = "^(publish|unpublish|cancel)$",
            message = "class.lifecycle.action.invalid")
    private String action;

    @Size(max = 500, message = "class.lifecycle.reason.size")
    private String reason;
}
