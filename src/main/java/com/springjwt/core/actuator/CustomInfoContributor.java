package com.springjwt.core.actuator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Info Contributor for Actuator
 * Adds custom application information to /actuator/info endpoint
 * Reads feature status from application configuration
 */
@Component
public class CustomInfoContributor implements InfoContributor {

    @Value("${spring.application.name:Spring Boot JWT API}")
    private String appName;

    @Value("${app.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${app.quartz.enabled:false}")
    private boolean quartzEnabled;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${server.port:8080}")
    private int serverPort;

    @Override
    public void contribute(Info.Builder builder) {
        // Application Information
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", appName);
        appInfo.put("description", "Base project with JWT authentication, Redis caching, and MySQL database");
        appInfo.put("version", "1.0.0");
        appInfo.put("profile", activeProfile);
        appInfo.put("port", serverPort);
        appInfo.put("timestamp", Instant.now().toString());

        // Features Status (from configuration)
        Map<String, String> features = new HashMap<>();
        features.put("jwt-authentication", "enabled");  // Always enabled
        features.put("redis-caching", redisEnabled ? "enabled" : "disabled");
        features.put("kafka-messaging", kafkaEnabled ? "enabled" : "disabled");
        features.put("quartz-scheduler", quartzEnabled ? "enabled" : "disabled");
        features.put("websocket", "enabled");
        features.put("file-upload", "enabled");
        features.put("i18n", "enabled");

        builder.withDetail("application", appInfo);
        builder.withDetail("features", features);
    }
}
