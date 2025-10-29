package com.springjwt.module.auth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Login response with tokens and user information")
public record LoginResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,

        @Schema(description = "Token type", example = "Bearer")
        String type,

        @Schema(description = "Authenticated user information")
        UserInfo user
) {
    // Compact constructor with default value for type
    public LoginResponse(String accessToken, String refreshToken, UserInfo user) {
        this(accessToken, refreshToken, "Bearer", user);
    }

    @Schema(description = "User information")
    public record UserInfo(
            @Schema(description = "Username", example = "admin")
            String username,

            @Schema(description = "Email address", example = "admin@example.com")
            String email,

            @Schema(description = "User roles", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
            List<String> roles
    ) {
    }
}
