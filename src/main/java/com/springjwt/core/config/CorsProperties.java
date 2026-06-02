package com.springjwt.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS configuration properties loaded from application.yml
 * Accessible via app.cors.* configuration keys
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * Allowed origins for CORS requests
     * Can be a list of URLs or "*" for all origins
     */
    private List<String> allowedOrigins;

    /**
     * Allowed HTTP methods
     * Default: GET, POST, PUT, DELETE, PATCH
     */
    private String allowedMethods = "GET,POST,PUT,DELETE,PATCH";

    /**
     * Allowed request headers
     * Default: Origin, Content-Type, Accept, Authorization
     */
    private String allowedHeaders = "Origin,Content-Type,Accept,Authorization";

    /**
     * Headers exposed to the client
     * Default: Authorization
     */
    private String exposedHeaders = "Authorization";

    /**
     * Whether credentials (cookies, authorization headers) should be included in CORS requests
     * Default: true
     */
    private Boolean allowCredentials = true;

    /**
     * Max age for CORS preflight request caching in seconds
     * Default: 3600 (1 hour)
     */
    private Long maxAge = 3600L;
}

