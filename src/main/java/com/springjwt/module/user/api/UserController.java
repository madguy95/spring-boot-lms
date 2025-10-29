package com.springjwt.module.user.api;

import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.user.business.UserService;
import com.springjwt.module.user.model.request.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User registration and management APIs")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with username, email, password, and optional roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Create new user's account
        userService.registerUser(signUpRequest);

        return ResponseFactory.success("User registered successfully!");
    }
}

