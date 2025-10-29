package com.springjwt.module.auth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for refresh token operation
 */
@Schema(description = "Response with new access and refresh tokens")
public record RefreshTokenResponse(
        @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "New JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,

        @Schema(description = "Token type", example = "Bearer")
        String type
) {
    // Compact constructor with default value for type
    public RefreshTokenResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}
