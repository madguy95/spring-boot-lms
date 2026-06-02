package com.springjwt.module.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Change password request")
public class ChangePasswordRequest {

    @NotBlank(message = "validation.currentPassword.required")
    @Schema(description = "Current password", example = "oldpassword123")
    private String currentPassword;

    @NotBlank(message = "validation.newPassword.required")
    @Size(min = 4, max = 40, message = "validation.password.size")
    @Schema(description = "New password (6-40 characters)", example = "newpassword123")
    private String newPassword;

    @NotBlank(message = "validation.confirmPassword.required")
    @Schema(description = "Confirm new password", example = "newpassword123")
    private String confirmPassword;
}
