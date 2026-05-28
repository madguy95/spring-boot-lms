package com.springjwt.module.auth.api;

import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.auth.business.AuthService;
import com.springjwt.module.auth.model.request.LoginRequest;
import com.springjwt.module.auth.model.request.RefreshTokenRequest;
import com.springjwt.module.auth.model.response.LoginResponse;
import com.springjwt.module.auth.model.response.RefreshTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/")
    @Operation(summary = "User sign in", description = "Authenticate user with phone and password. Returns access token and refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            return ResponseFactory.success(response);
        } catch (BadCredentialsException e) {
            return ResponseFactory.error(HttpStatus.BAD_REQUEST, "Invalid phone or password");
        } catch (Exception e) {
            log.error("Authentication failed", e);
            return ResponseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication failed");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token and refresh token using valid refresh token from Redis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshTokenResponse response = authService.refreshToken(request.refreshToken());
            return ResponseFactory.success(response);
        } catch (BadCredentialsException e) {
            return ResponseFactory.error(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            return ResponseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to refresh token");
        }
    }

    @PostMapping("/revoke")
    @Operation(summary = "Revoke token", description = "Invalidate both access token and refresh token for the authenticated user.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token revoked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Failed to revoke token")
    })
    public ResponseEntity<?> revokeToken(
            @Parameter(description = "Bearer token in Authorization header", required = true)
            @RequestHeader("Authorization") String token,
            Authentication auth
    ) {
        try {
            String jwt = token.replace("Bearer ", "");
            boolean revoked = authService.revokeToken(auth, jwt);

            return revoked
                    ? ResponseFactory.success("Token revoked successfully")
                    : ResponseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to revoke token");
        } catch (Exception e) {
            log.error("Failed to revoke token", e);
            return ResponseFactory.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to revoke token");
        }
    }
}
