package com.springjwt.module.auth.model.request;

import com.springjwt.common.annotation.StrProc;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request with username and password")
public class LoginRequest {
    @NotBlank(message = "validation.username.required")
    @StrProc
    @Schema(description = "Username for authentication", example = "admin")
    private String username;

    @NotBlank(message = "validation.password.required")
    @StrProc
    @Schema(description = "Password for authentication", example = "password123")
    private String password;
}
