package com.springjwt.module.auth.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refresh token
 */
@Schema(description = "Request to refresh access token")
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        @Schema(description = "Valid refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {
}
