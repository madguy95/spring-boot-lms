package com.springjwt.core.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Topics Configuration
 * <p>
 * This configuration class manages Kafka topic creation and configuration.
 * Topics are automatically created when the application starts if they don't exist.
 * <p>
 * Configuration properties:
 * - kafka.topics.email-queue.* : Email queue topic settings
 * - kafka.topics.dlt.* : Dead Letter Topic settings
 * - kafka.dlq.enabled : Enable/disable DLQ functionality
 * - kafka.dlq.suffix : Suffix for DLT topic names (default: .DLT)
 *
 * @see KafkaConfig for producer/consumer configuration
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopics {

    // ===== Topic Names =====
    public static final String EMAIL_QUEUE = "email-queue";

    // ===== Email Queue Topic Configuration =====
    /**
     * Number of partitions for email queue topic
     * More partitions = higher parallelism and throughput
     * Default: 3
     */
    @Value("${kafka.topics.email-queue.partitions:3}")
    private int emailQueuePartitions;

    /**
     * Replication factor for email queue topic
     * Higher replication = better fault tolerance
     * Default: 1 (for dev), should be 3 in production
     */
    @Value("${kafka.topics.email-queue.replication-factor:1}")
    private int emailQueueReplicationFactor;

    /**
     * Retention period for email queue messages in milliseconds
     * Default: 86400000ms = 24 hours
     * Messages older than this will be deleted
     */
    @Value("${kafka.topics.email-queue.retention-ms:86400000}")
    private long emailQueueRetentionMs;

    // ===== DLT Topic Configuration =====
    /**
     * Number of partitions for Dead Letter Topic
     * Usually 1 is enough as failed messages are rare
     * Default: 1
     */
    @Value("${kafka.topics.dlt.partitions:1}")
    private int dltPartitions;

    /**
     * Replication factor for Dead Letter Topic
     * Should match main topic replication for consistency
     * Default: 1 (for dev), should be 3 in production
     */
    @Value("${kafka.topics.dlt.replication-factor:1}")
    private int dltReplicationFactor;

    /**
     * Retention period for DLT messages in milliseconds
     * Default: 604800000ms = 7 days
     * Keep failed messages longer for investigation
     */
    @Value("${kafka.topics.dlt.retention-ms:604800000}")
    private long dltRetentionMs;

    /**
     * Enable/disable Dead Letter Queue functionality
     * When disabled, failed messages will not be sent to DLT
     * Default: true
     */
    @Value("${kafka.dlq.enabled:true}")
    private boolean dlqEnabled;

    /**
     * Suffix to append to topic names for DLT
     * Example: email-queue -> email-queue.DLT
     * Default: .DLT
     */
    @Value("${kafka.dlq.suffix:.DLT}")
    private String dlqSuffix;

    // ===== Topic Bean Definitions =====

    /**
     * Create email queue topic
     * This topic is used for asynchronous email sending
     *
     * @return NewTopic configuration for email queue
     */
    @Bean
    public NewTopic emailQueueTopic() {
        log.info("Creating Kafka topic: {} with {} partitions and {} replicas",
                EMAIL_QUEUE, emailQueuePartitions, emailQueueReplicationFactor);
        return TopicBuilder.name(EMAIL_QUEUE)
                .partitions(emailQueuePartitions)
                .replicas(emailQueueReplicationFactor)
                .config("retention.ms", String.valueOf(emailQueueRetentionMs))
                .build();
    }

    /**
     * Create Dead Letter Topic for email queue
     * Failed messages will be sent here after all retry attempts
     * Only created if DLQ is enabled
     *
     * @return NewTopic configuration for DLT, or null if DLQ is disabled
     */
    @Bean
    public NewTopic emailQueueDltTopic() {
        String EMAIL_QUEUE_DLT = EMAIL_QUEUE + dlqSuffix;

        // Skip DLT creation if DLQ is disabled
        if (!dlqEnabled) {
            log.info("DLT topics are disabled. Skipping creation of topic: {}", EMAIL_QUEUE_DLT);
            return null;
        }

        log.info("Creating Kafka DLT topic: {} with {} partitions and {} replicas",
                EMAIL_QUEUE_DLT, dltPartitions, dltReplicationFactor);
        return TopicBuilder.name(EMAIL_QUEUE_DLT)
                .partitions(dltPartitions)
                .replicas(dltReplicationFactor)
                .config("retention.ms", String.valueOf(dltRetentionMs))
                .build();
    }
}

