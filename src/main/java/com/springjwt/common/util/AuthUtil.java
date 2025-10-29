package com.springjwt.common.util;

import com.springjwt.module.auth.model.dto.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthUtil {

    /**
     * Get current user ID
     * Returns:
     * - "system" if no authentication context (background jobs, scheduled tasks)
     * - "anonymous" if user is not logged in (AnonymousAuthenticationToken)
     * - User ID (as String) if user is authenticated
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Case 1: No authentication context (system tasks)
        if (authentication == null) {
            return "system";
        }

        // Case 2: Anonymous user (not logged in)
        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }

        // Case 3: Authenticated user
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId().toString();
        }

        return "system";
    }

    /**
     * Get current username
     * Returns:
     * - "system" if no authentication context (background jobs, scheduled tasks)
     * - "anonymous" if user is not logged in (AnonymousAuthenticationToken)
     * - Username if user is authenticated
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Case 1: No authentication context (system tasks)
        if (authentication == null) {
            return "system";
        }

        // Case 2: Anonymous user (not logged in)
        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }

        // Case 3: Authenticated user
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUsername();
        }

        // Fallback: Use authentication name
        return authentication.getName() != null ? authentication.getName() : "system";
    }

    /**
     * Get current HTTP request from RequestContextHolder
     */
    public static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get client IP address from request
     * Handles proxy headers (X-Forwarded-For, X-Real-IP)
     */
    public static String getClientIP(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    /**
     * Check if current user is authenticated (not anonymous)
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Check if current user is anonymous
     */
    public static boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AnonymousAuthenticationToken;
    }

    /**
     * Check if current context is system (no authentication)
     */
    public static boolean isSystem() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !authentication.isAuthenticated();
    }
}

