package com.springjwt.core.actuator;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom Health Indicator for Kafka
 *
 * This health indicator monitors the connectivity and availability of Kafka cluster.
 * It provides real-time health status that can be accessed via Spring Boot Actuator endpoints.
 *
 * Health check process:
 * 1. Create temporary AdminClient from KafkaAdmin configuration
 * 2. Attempt to describe the Kafka cluster
 * 3. Retrieve cluster ID and node count
 * 4. Return UP status with cluster info if successful
 * 5. Return DOWN status with error details if failed
 *
 * Configuration properties:
 * - kafka.health.enabled : Enable/disable this health indicator
 * - kafka.health.timeout : Timeout for health check in milliseconds (default: 5000ms)
 *
 * Health endpoint: GET /actuator/health
 *
 * @see KafkaAdminConfig for KafkaAdmin bean configuration
 */
@Slf4j
@Component("kafka")
@ConditionalOnProperty(name = "kafka.health.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    /**
     * Timeout for health check operations in milliseconds
     * Prevents health check from hanging indefinitely
     * Default: 5000ms (5 seconds)
     */
    @Value("${kafka.health.timeout:5000}")
    private long healthCheckTimeout;

    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    /**
     * Perform health check on Kafka cluster
     *
     * This method is called by Spring Boot Actuator to check Kafka health status.
     *
     * Returns:
     * - Health.up() : Kafka cluster is accessible and responsive
     *   - Details: clusterId, nodeCount, status
     * - Health.down() : Kafka cluster is not accessible
     *   - Details: error type, error message, status
     *
     * @return Health status of Kafka cluster
     */
    @Override
    public Health health() {
        try {
            // Create AdminClient from KafkaAdmin configuration
            try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {

                // Set timeout for health check to prevent hanging
                DescribeClusterOptions options = new DescribeClusterOptions()
                        .timeoutMs((int) healthCheckTimeout);

                // Try to describe cluster to verify connection
                var clusterDescription = adminClient.describeCluster(options);

                // Get cluster ID and node count (will throw exception if connection fails)
                String clusterId = clusterDescription.clusterId().get(healthCheckTimeout, TimeUnit.MILLISECONDS);
                int nodeCount = clusterDescription.nodes().get(healthCheckTimeout, TimeUnit.MILLISECONDS).size();

                log.debug("Kafka health check passed - Cluster ID: {}, Nodes: {}", clusterId, nodeCount);

                return Health.up()
                        .withDetail("clusterId", clusterId)
                        .withDetail("nodeCount", nodeCount)
                        .withDetail("status", "Connected to Kafka broker")
                        .build();
            }
        } catch (Exception e) {
            log.error("Kafka health check failed: {}", e.getMessage());

            return Health.down()
                    .withDetail("error", e.getClass().getSimpleName())
                    .withDetail("message", e.getMessage())
                    .withDetail("status", "Unable to connect to Kafka broker")
                    .build();
        }
    }
}

