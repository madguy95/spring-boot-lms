package com.springjwt.module.auth.business;

import com.springjwt.core.security.jwt.JwtUtils;
import com.springjwt.module.auth.model.dto.UserPrincipal;
import com.springjwt.module.auth.model.request.LoginRequest;
import com.springjwt.module.auth.model.response.LoginResponse;
import com.springjwt.module.auth.model.response.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    /**
     * Authenticate user and generate access + refresh tokens
     */
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateJwtRefreshToken(authentication);

            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            log.info("User authenticated successfully: {}", loginRequest.getUsername());
            return new LoginResponse(
                    jwt,
                    refreshToken,
                    new LoginResponse.UserInfo(userDetails.getUsername(), userDetails.getEmail(), roles)
            );

        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * Refresh access token using refresh token
     * Validates refresh token and generates new access + refresh tokens
     */
    public RefreshTokenResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            log.error("Invalid or expired refresh token");
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
        UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtUtils.generateJwtToken(authentication);
        String newRefreshToken = jwtUtils.generateJwtRefreshToken(authentication);

        log.info("Token refreshed successfully for user: {}", username);
        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    /**
     * Revoke token and invalidate refresh token
     */
    public boolean revokeToken(Authentication auth, String token) {
        boolean revoked = jwtUtils.revokeToken(auth, token);
        if (revoked) {
            log.info("Token revoked successfully for user: {}", auth.getName());
        }
        return revoked;
    }
}