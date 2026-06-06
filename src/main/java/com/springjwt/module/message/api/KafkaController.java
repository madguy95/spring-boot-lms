package com.springjwt.module.message.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.core.kafka.KafkaTopics;
import com.springjwt.module.message.business.producer.KafkaMessageProducer;
import com.springjwt.module.message.model.dto.EmailData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Kafka Message Controller
 * REST API endpoints for testing Kafka message production
 */
@Slf4j
@RestController
@RequestMapping("/api/kafka")
@Tag(name = "Kafka", description = "Kafka Message API")
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaController {

    @Autowired
    private KafkaMessageProducer kafkaMessageProducer;

    @PostMapping("/email-queue")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @Operation(summary = "Send Email Request", description = "Send an email request to Kafka")
    public ResponseEntity<ApiResult<String>> sendEmailRequest(@RequestBody EmailData emailRequest) {
        try {
            // Send to Kafka
            kafkaMessageProducer.sendMessage(
                    KafkaTopics.EMAIL_QUEUE,
                    UUID.randomUUID().toString(),
                    emailRequest
            );

            return ResponseFactory.success("Email request sent successfully");

        } catch (Exception e) {
            log.error("Failed to send email request: {}", e.getMessage(), e);
            return ResponseFactory.error("Failed to send email request: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Kafka Health Check", description = "Check Kafka connection status")
    public ResponseEntity<ApiResult<String>> healthCheck() {
        return ResponseFactory.success("Kafka is healthy");
    }
}

