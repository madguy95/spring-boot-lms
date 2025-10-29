package com.springjwt.core.security.jwt;

import com.springjwt.core.redis.RedisService;
import com.springjwt.module.auth.model.dto.UserPrincipal;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtils {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String REVOKED_TOKEN_PREFIX = "revoked_token:";

    private SecretKey SECRET_KEY;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${app.jwtRefreshExpirationMs}")
    private long jwtRefreshExpirationMs;

    @Autowired(required = false)
    private RedisService service;

    @PostConstruct
    public void generateSignInKey() {
        this.SECRET_KEY = new SecretKeySpec(Base64.getDecoder().decode(jwtSecret), "HmacSHA256");
        if (service == null) {
            log.warn("RedisService is not available. Token revocation and refresh token features will be disabled.");
        }
    }

    public String parseJwt(Object request) {
        String headerAuth = null;

        if (request instanceof HttpServletRequest httpRequest) {
            headerAuth = httpRequest.getHeader("Authorization");
        }
        if (request instanceof StompHeaderAccessor stompAccessor) {
            List<String> authorization = stompAccessor.getNativeHeader("Authorization");
            if (!CollectionUtils.isEmpty(authorization)) {
                headerAuth = authorization.getFirst();
            }
        }

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return headerAuth;
    }

    /**
     * Generate access token for authenticated user
     */
    public String generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenForUser(userPrincipal.getUsername(), jwtExpirationMs, "access");
    }

    /**
     * Generate refresh token for authenticated user and store in Redis
     */
    public String generateJwtRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String refreshToken = generateTokenForUser(userPrincipal.getUsername(), jwtRefreshExpirationMs, "refresh");

        // Store refresh token in Redis with TTL (only if Redis is available)
        if (service != null) {
            String key = REFRESH_TOKEN_PREFIX + userPrincipal.getUsername();
            service.setValue(key, refreshToken, TimeUnit.MILLISECONDS, jwtRefreshExpirationMs, false);
        } else {
            log.warn("RedisService not available. Refresh token not stored in cache.");
        }

        return refreshToken;
    }

    /**
     * Optimized: Common token generation logic to reduce code duplication
     */
    private String generateTokenForUser(String username, long expirationMs, String tokenType) {
        String hash = getHash(username);
        Claims claims = Jwts.claims()
                .add("username", username)
                .add("hash", hash)
                .add("type", tokenType)
                .build();

        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return (String) Jwts.parser().verifyWith(SECRET_KEY)
                .build().parseSignedClaims(token).getPayload().get("username");
    }

    /**
     * Optimized: Revoke token and clean up refresh token
     */
    public boolean revokeToken(Authentication auth, String token) {
        if (service == null) {
            log.warn("RedisService not available. Token revocation is disabled.");
            return false;
        }

        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY)
                    .build().parseSignedClaims(token).getPayload();

            if (claims != null && claims.containsKey("username") && claims.containsKey("hash")) {
                String username = claims.get("username").toString();
                String hash = claims.get("hash").toString();

                LocalDateTime now = LocalDateTime.now();
                Duration aliveTime = Duration.between(now,
                        claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

                // Store revoked token with prefix
                String key = String.format("%s%s_%s", REVOKED_TOKEN_PREFIX, username, hash);
                service.setValue(key, auth.getPrincipal(), TimeUnit.SECONDS, aliveTime.getSeconds(), true);

                // Also invalidate refresh token
                service.removeKey(REFRESH_TOKEN_PREFIX + username);
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to revoke token", e);
        }
        return false;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY)
                    .build().parseSignedClaims(authToken).getPayload();

            if (claims != null && claims.containsKey("username") && claims.containsKey("hash")) {
                // Check revocation only if Redis is available
                if (service != null) {
                    String username = claims.get("username").toString();
                    String hash = claims.get("hash").toString();
                    String key = String.format("%s%s_%s", REVOKED_TOKEN_PREFIX, username, hash);

                    if (service.getValue(key) != null) {
                        log.error("Token has been revoked");
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception e) {
            String errorMessage = switch (e) {
                case MalformedJwtException ex -> "Invalid JWT token: " + ex.getMessage();
                case ExpiredJwtException ex -> "JWT token is expired: " + ex.getMessage();
                case UnsupportedJwtException ex -> "JWT token is unsupported: " + ex.getMessage();
                case IllegalArgumentException ex -> "JWT claims string is empty: " + ex.getMessage();
                default -> "Unknown JWT error: " + e.getMessage();
            };
            log.error(errorMessage);
        }
        return false;
    }

    /**
     * New: Validate refresh token against stored token in Redis
     */
    public boolean validateRefreshToken(String refreshToken) {
        if (service == null) {
            log.warn("RedisService not available. Refresh token validation is disabled.");
            return false;
        }

        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY)
                    .build().parseSignedClaims(refreshToken).getPayload();

            if (claims == null || !claims.containsKey("username") || !claims.containsKey("type")) {
                return false;
            }

            String tokenType = claims.get("type").toString();
            if (!"refresh".equals(tokenType)) {
                log.error("Token is not a refresh token");
                return false;
            }

            String username = claims.get("username").toString();
            Object storedToken = service.getValue(REFRESH_TOKEN_PREFIX + username);

            return refreshToken.equals(storedToken);
        } catch (Exception ex) {
            log.error("Invalid refresh token: {}", ex.getMessage());
        }
        return false;
    }

    public String getHash(String username) {
        return DigestUtils.md5DigestAsHex(String.format("%s_%d", username, new Date().getTime()).getBytes());
    }
}

