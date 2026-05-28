package com.springjwt.module.auth.model.request;

import com.springjwt.common.annotation.StrProc;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request with phone and password")
public class LoginRequest {
    @NotBlank(message = "validation.phone.required")
    @StrProc
    @Schema(description = "Phone for authentication", example = "0901234567")
    private String phone;

    @NotBlank(message = "validation.password.required")
    @StrProc
    @Schema(description = "Password for authentication", example = "password123")
    private String password;
}
