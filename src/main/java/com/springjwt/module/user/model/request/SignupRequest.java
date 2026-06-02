package com.springjwt.module.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration request")
public class SignupRequest {

    @NotBlank(message = "validation.username.required")
    @Size(min = 3, max = 20, message = "validation.username.size")
    @Schema(description = "Username (3-20 characters)", example = "newuser")
    private String username;

    @NotBlank(message = "validation.email.required")
    @Size(max = 50, message = "validation.email.size")
    @Email(message = "validation.email.invalid")
    @Schema(description = "Email address", example = "newuser@example.com")
    private String email;

    @NotBlank(message = "validation.phone.required")
    @Size(max = 20, message = "validation.phone.size")
    @Schema(description = "Phone number", example = "0901234567")
    private String phone;

    @Schema(description = "User roles", example = "[\"user\", \"admin\"]")
    private Set<String> roles;

    @NotBlank(message = "validation.password.required")
    @Size(min = 4, max = 40, message = "validation.password.size")
    @Schema(description = "Password (6-40 characters)", example = "password123")
    private String password;
}
