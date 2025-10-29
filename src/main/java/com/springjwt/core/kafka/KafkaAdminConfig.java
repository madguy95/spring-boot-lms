package com.springjwt.core.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Admin Configuration
 *
 * This configuration creates KafkaAdmin bean for administrative operations
 * and health checks. Only active when health check is enabled.
 *
 * KafkaAdmin is used for:
 * - Health check monitoring (via KafkaHealthIndicator)
 * - Topic management and verification
 * - Cluster information retrieval
 *
 * Configuration properties:
 * - kafka.health.enabled : Enable/disable this configuration
 * - spring.kafka.bootstrap-servers : Kafka broker addresses
 *
 * @see KafkaHealthIndicator for health check implementation
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "kafka.health.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaAdminConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaAdminConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * Create KafkaAdmin bean for administrative operations
     *
     * This bean provides access to Kafka AdminClient which can:
     * - Describe cluster (nodes, controller, cluster ID)
     * - Create/delete topics
     * - List topics and partitions
     * - Check broker connectivity
     *
     * Used primarily by KafkaHealthIndicator to monitor cluster health
     *
     * @return KafkaAdmin instance configured with bootstrap servers
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                    kafkaProperties.getBootstrapServers());

        log.info("========================================");
        log.info("Kafka Admin Configuration:");
        log.info("Bootstrap Servers: {}", kafkaProperties.getBootstrapServers());
        log.info("Kafka Admin created for health checks and administrative operations");
        log.info("========================================");

        return new KafkaAdmin(configs);
    }
}

