package com.springjwt.core.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.TRANSACTIONAL_ID_CONFIG;
import static org.springframework.kafka.support.serializer.JsonSerializer.ADD_TYPE_INFO_HEADERS;

/**
 * Kafka Configuration
 * <p>
 * Main configuration class for Kafka producer, consumer, and error handling.
 * This configuration uses Spring Boot auto-configuration combined with custom settings.
 * <p>
 * Key features:
 * - Dual Producers: Non-Transactional (default, for high throughput) and Transactional (for critical operations)
 * - Multiple Consumers: Default consumer and Email-specific consumer with type mappings
 * - Error Handling: Exponential backoff retry + Dead Letter Queue
 * - Monitoring: Metrics and logging enabled
 * <p>
 * Configuration sources:
 * - spring.kafka.* : Standard Spring Kafka properties (auto-loaded)
 * - kafka.error.* : Custom retry/backoff settings
 * - kafka.dlq.* : Dead Letter Queue settings
 *
 * @see KafkaTopics for topic configuration
 * @see KafkaAdminConfig for admin/health check configuration
 */
@Slf4j
@EnableKafka
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    // ===== Error Handling Configuration =====
    // These properties control retry behavior when message processing fails

    /**
     * Maximum number of retry attempts before sending to DLQ
     * Default: 3 attempts
     */
    @Value("${kafka.error.max-attempts:3}")
    private int maxAttempts;

    /**
     * Initial backoff interval in milliseconds between retry attempts
     * Default: 1000ms (1 second)
     */
    @Value("${kafka.error.backoff-interval:1000}")
    private long backoffInterval;

    /**
     * Multiplier for exponential backoff
     * Each retry waits: interval * (multiplier ^ attempt)
     * Default: 2.0 (doubles each time: 1s, 2s, 4s, 8s...)
     */
    @Value("${kafka.error.backoff-multiplier:2.0}")
    private double backoffMultiplier;

    /**
     * Maximum backoff interval in milliseconds
     * Prevents backoff from growing too large
     * Default: 10000ms (10 seconds)
     */
    @Value("${kafka.error.max-backoff-interval:10000}")
    private long maxBackoffInterval;

    // ===== Dead Letter Queue Configuration =====

    /**
     * Enable/disable sending failed messages to DLQ
     * When disabled, failed messages are only logged
     * Default: true
     */
    @Value("${kafka.dlq.enabled:true}")
    private boolean dlqEnabled;

    /**
     * Suffix to append to original topic name for DLQ
     * Example: email-queue -> email-queue.DLT
     * Default: .DLT
     */
    @Value("${kafka.dlq.suffix:.DLT}")
    private String dlqSuffix;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // ===== Producer Configuration =====

    /**
     * Configure Transactional Kafka Producer Factory
     * <p>
     * This producer is configured with transactions for critical operations
     * that require exactly-once semantics and atomicity.
     * <p>
     * IMPORTANT: Transaction is enabled by setting transactionIdPrefix.
     * Without this, the producer cannot participate in transactions.
     * <p>
     * Features:
     * - Transactions: Atomic message sending (exactly-once delivery)
     * - Idempotence: Prevents duplicate messages
     * - Compression: Reduces network bandwidth (snappy)
     * - Batching: Improves throughput
     * <p>
     * Use cases:
     * - Financial transactions
     * - Order processing
     * - Critical data that must not be duplicated or lost
     * - Operations requiring atomicity across multiple topics
     * <p>
     * All settings from spring.kafka.producer.* in yml are automatically loaded
     *
     * @return ProducerFactory configured for transactional operations
     */
    @Bean
    public ProducerFactory<String, Object> transactionalProducerFactory() {
        // Load all configurations from spring.kafka.producer in yaml
        var configs = kafkaProperties.buildProducerProperties(null);

        // Create producer factory with transaction support
        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(configs);

        // CRITICAL: Enable transaction by setting transaction ID prefix
        // This is REQUIRED for transactions to work
        String transactionIdPrefix = (String) configs.get(TRANSACTIONAL_ID_CONFIG);
        if (transactionIdPrefix != null && !transactionIdPrefix.isEmpty()) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
            log.info("========================================");
            log.info("Kafka Transactional Producer Configuration:");
            log.info("Bootstrap Servers: {}", kafkaProperties.getBootstrapServers());
            log.info("Transaction ID Prefix: {}", transactionIdPrefix);
            log.info("Acks: {}", kafkaProperties.getProducer().getAcks());
            log.info("Enable Idempotence: {}", configs.get(ENABLE_IDEMPOTENCE_CONFIG));
            log.info("Add Type Headers: {}", configs.get(ADD_TYPE_INFO_HEADERS));
            log.info("Transaction Support: ENABLED");
            log.info("========================================");
        } else {
            log.warn("========================================");
            log.warn("WARNING: Transaction ID not configured!");
            log.warn("Transactional features will NOT work.");
            log.warn("Please set: spring.kafka.producer.properties.transactional.id");
            log.warn("========================================");
        }

        return factory;
    }

    /**
     * Configure Non-Transactional Kafka Producer Factory
     * <p>
     * This producer is configured WITHOUT transactions for high-throughput operations
     * where eventual consistency is acceptable.
     * <p>
     * Features:
     * - No transactions: Better performance and throughput
     * - Idempotence: Still prevents duplicates within a session
     * - Compression: Reduces network bandwidth (snappy)
     * - Batching: Improves throughput
     * - Lower latency: No transaction coordination overhead
     * <p>
     * Use cases:
     * - Logging and metrics
     * - Analytics events
     * - Non-critical notifications
     * - High-volume data streams
     * - Real-time monitoring data
     *
     * @return ProducerFactory configured for non-transactional operations
     */
    @Bean
    public ProducerFactory<String, Object> nonTransactionalProducerFactory() {
        // Load base configurations from spring.kafka.producer in yaml
        var configs = kafkaProperties.buildProducerProperties(null);

        // Remove transactional settings for better performance
        configs.remove(TRANSACTIONAL_ID_CONFIG);
        configs.remove("transaction.timeout.ms");

        log.info("========================================");
        log.info("Kafka Non-Transactional Producer Configuration:");
        log.info("Bootstrap Servers: {}", kafkaProperties.getBootstrapServers());
        log.info("Acks: {}", kafkaProperties.getProducer().getAcks());
        log.info("Enable Idempotence: {}", configs.get(ENABLE_IDEMPOTENCE_CONFIG));
        log.info("Add Type Headers: {}", configs.get(ADD_TYPE_INFO_HEADERS));
        log.info("Transaction Support: DISABLED (for better performance)");
        log.info("========================================");

        return new DefaultKafkaProducerFactory<>(configs);
    }

    /**
     * Create Transactional KafkaTemplate (Bean name: txKafkaTemplate)
     * <p>
     * Use this template ONLY for critical operations that require exactly-once semantics.
     * <p>
     * NOTE: This is NOT the default template. In KafkaMessageProducer, nonTxKafkaTemplate
     * is used as the default for better performance. Use txKafkaTemplate explicitly with
     *
     * @return Transactional KafkaTemplate instance
     * @Qualifier("txKafkaTemplate") for transactional operations.
     * <p>
     * Example usage:
     * <pre>
     * {@code
     * @Autowired
     * @Qualifier("txKafkaTemplate")
     * private KafkaTemplate<String, Object> txTemplate;
     *
     * // Execute in transaction
     * txTemplate.executeInTransaction(operations -> {
     *     operations.send("topic1", message1);
     *     operations.send("topic2", message2);
     *     return true; // Commit both or rollback both
     * });
     * }
     * </pre>
     */
    @Bean(name = "txKafkaTemplate")
    public KafkaTemplate<String, Object> txKafkaTemplate() {
        return new KafkaTemplate<>(transactionalProducerFactory());
    }

    /**
     * Create Non-Transactional KafkaTemplate (Bean names: kafkaTemplate, nonTxKafkaTemplate)
     * <p>
     * This is the RECOMMENDED DEFAULT template for most use cases.
     * Use this template for high-throughput operations where eventual consistency
     * is acceptable and you need better performance without transaction overhead.
     * <p>
     * NOTE: This bean has TWO names:
     * - "kafkaTemplate" (primary/default name)
     * - "nonTxKafkaTemplate" (explicit qualifier name)
     * <p>
     * In KafkaMessageProducer, this template is used as the default for all
     * standard message sending operations (sendMessage, sendMessageSync).
     * Only methods with "InTransaction" suffix use txKafkaTemplate.
     * <p>
     * Example usage:
     * <pre>
     * {@code
     * // Option 1: Use default name (autowired without qualifier)
     * @Autowired
     * private KafkaTemplate<String, Object> kafkaTemplate;
     *
     * // Option 2: Use explicit qualifier
     * @Autowired
     * @Qualifier("nonTxKafkaTemplate")
     * private KafkaTemplate<String, Object> nonTxTemplate;
     *
     * // Send message without transaction overhead (faster)
     * nonTxTemplate.send("analytics-topic", event);
     * nonTxTemplate.send("logs-topic", logEvent);
     * }
     * </pre>
     * <p>
     * Performance benefit:
     * - ~30-50% faster than transactional producer
     * - Lower CPU and memory usage
     * - Better for high-frequency, low-criticality messages
     * - Default choice for most business operations
     *
     * @return Non-Transactional KafkaTemplate instance
     */
    @Bean(name = {"kafkaTemplate", "nonTxKafkaTemplate"})
    public KafkaTemplate<String, Object> nonTxKafkaTemplate() {
        return new KafkaTemplate<>(nonTransactionalProducerFactory());
    }

    // ===== Consumer Configuration =====

    /**
     * Configure Default Kafka Consumer Factory
     * <p>
     * Default consumer factory for general purpose message consumption.
     * Uses generic Object deserialization with type headers.
     * <p>
     * Features:
     * - Manual acknowledgment: Fine-grained control over offset commits
     * - Batch processing: Process multiple messages efficiently
     * - JSON deserialization: Automatic object mapping with type headers
     * - Type mapping: Supports custom type mappings from configuration
     * <p>
     * All settings from spring.kafka.consumer.* in yml are automatically loaded
     *
     * @return ConsumerFactory configured for String keys and Object values
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        // Load all configurations from spring.kafka.consumer in yaml
        var configs = kafkaProperties.buildConsumerProperties(null);
        log.info("========================================");
        log.info("Kafka Default Consumer Configuration:");
        log.info("Bootstrap Servers: {}", kafkaProperties.getBootstrapServers());
        log.info("Group ID: {}", kafkaProperties.getConsumer().getGroupId());
        log.info("Auto Offset Reset: {}", kafkaProperties.getConsumer().getAutoOffsetReset());
        log.info("Enable Auto Commit: {}", kafkaProperties.getConsumer().getEnableAutoCommit());
        log.info("Use Type Headers: {}", configs.get(ADD_TYPE_INFO_HEADERS));
        log.info("Trusted Packages: {}", configs.get(JsonDeserializer.TRUSTED_PACKAGES));
        log.info("Type Mappings: {}", configs.get(JsonDeserializer.TYPE_MAPPINGS));
        log.info("Default Value Type: {}", configs.get(JsonDeserializer.VALUE_DEFAULT_TYPE));
        log.info("========================================");

        return new DefaultKafkaConsumerFactory<>(configs);
    }

    /**
     * Configure Default Kafka Listener Container Factory
     * <p>
     * This factory creates containers that manage @KafkaListener instances
     * for general purpose message consumption.
     * <p>
     * Features:
     * - Concurrency: Multiple consumer threads per topic
     * - Manual acknowledgment: Explicit offset commit control
     * - Error handling: Integrated with exponential backoff retry
     * <p>
     * Concurrency and ack-mode are loaded from spring.kafka.listener.* in yaml
     *
     * @param errorHandler Custom error handler with retry logic
     * @return ConcurrentKafkaListenerContainerFactory instance
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Load concurrency and ack-mode from spring.kafka.listener in yaml
        var listenerProperties = kafkaProperties.getListener();

        if (listenerProperties.getConcurrency() != null) {
            factory.setConcurrency(listenerProperties.getConcurrency());
            log.info("Default Listener Concurrency: {}", listenerProperties.getConcurrency());
        }

        if (listenerProperties.getAckMode() != null) {
            factory.getContainerProperties().setAckMode(listenerProperties.getAckMode());
            log.info("Default Listener Ack Mode: {}", listenerProperties.getAckMode());
        }

        // Set custom error handler with exponential backoff retry
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }


    /**
     * Configure Email-Specific Kafka Consumer Factory
     * <p>
     * Specialized consumer factory for email message consumption.
     * Pre-configured with EmailData as the default value type for better performance.
     * <p>
     * Features:
     * - Type-specific deserialization: Direct EmailData object mapping
     * - No type header lookup: Faster deserialization
     * - Manual acknowledgment: Fine-grained control over offset commits
     * - JSON deserialization: Automatic EmailData object mapping
     * <p>
     * Use with @KafkaListener(containerFactory = "emailListenerContainerFactory")
     * <p>
     * All settings from spring.kafka.consumer.* in yml are automatically loaded
     *
     * @return ConsumerFactory configured for String keys and EmailData values
     */
    @Bean
    public ConsumerFactory<String, Object> emailConsumerFactory() {
        // Load all configurations from spring.kafka.consumer in yaml
        var configs = kafkaProperties.buildConsumerProperties(null);

        // Override default value type for email messages
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.springjwt.module.message.model.dto.EmailData");

        log.info("========================================");
        log.info("Kafka Email Consumer Configuration:");
        log.info("Bootstrap Servers: {}", kafkaProperties.getBootstrapServers());
        log.info("Group ID: {}", kafkaProperties.getConsumer().getGroupId());
        log.info("Auto Offset Reset: {}", kafkaProperties.getConsumer().getAutoOffsetReset());
        log.info("Enable Auto Commit: {}", kafkaProperties.getConsumer().getEnableAutoCommit());
        log.info("Use Type Headers: {}", configs.get(ADD_TYPE_INFO_HEADERS));
        log.info("Trusted Packages: {}", configs.get(JsonDeserializer.TRUSTED_PACKAGES));
        log.info("Type Mappings: {}", configs.get(JsonDeserializer.TYPE_MAPPINGS));
        log.info("Default Value Type: {}", configs.get(JsonDeserializer.VALUE_DEFAULT_TYPE));
        log.info("========================================");

        return new DefaultKafkaConsumerFactory<>(configs);
    }

    /**
     * Configure Email-Specific Kafka Listener Container Factory
     * <p>
     * This factory creates containers that manage @KafkaListener instances
     * specifically for email message consumption.
     * <p>
     * Features:
     * - Email-specific deserialization: Uses emailConsumerFactory
     * - Concurrency: Multiple consumer threads per topic
     * - Manual acknowledgment: Explicit offset commit control
     * - Error handling: Integrated with exponential backoff retry
     * <p>
     * Usage in listener:
     * <pre>
     * {@code
     * @KafkaListener(
     *     topics = "email-queue",
     *     containerFactory = "emailListenerContainerFactory"
     * )
     * public void handleEmail(EmailData email) {
     *     // Process email
     * }
     * }
     * </pre>
     * <p>
     * Concurrency and ack-mode are loaded from spring.kafka.listener.* in yaml
     *
     * @param errorHandler Custom error handler with retry logic
     * @return ConcurrentKafkaListenerContainerFactory instance
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> emailListenerContainerFactory(
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(emailConsumerFactory());

        // Load concurrency and ack-mode from spring.kafka.listener in yaml
        var listenerProperties = kafkaProperties.getListener();

        if (listenerProperties.getConcurrency() != null) {
            factory.setConcurrency(listenerProperties.getConcurrency());
            log.info("Email Listener Concurrency: {}", listenerProperties.getConcurrency());
        }

        if (listenerProperties.getAckMode() != null) {
            factory.getContainerProperties().setAckMode(listenerProperties.getAckMode());
            log.info("Email Listener Ack Mode: {}", listenerProperties.getAckMode());
        }

        // Set custom error handler with exponential backoff retry
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    // ===== Error Handler with Exponential Backoff =====

    /**
     * Configure Error Handler with Exponential Backoff and DLQ
     * <p>
     * Error handling strategy:
     * 1. Message processing fails
     * 2. Retry with exponential backoff (e.g., 1s, 2s, 4s, 8s...)
     * 3. After max attempts, send to Dead Letter Queue (DLQ)
     * 4. If DLQ disabled, just log the failed message
     * <p>
     * Retry timing example (with default config):
     * - Attempt 1: immediate
     * - Attempt 2: wait 1s
     * - Attempt 3: wait 2s
     * - Failed -> Send to DLQ
     * <p>
     * DLQ Topic Naming:
     * - Original topic: email-queue
     * - DLQ topic: email-queue.DLT (configurable via kafka.dlq.suffix)
     * <p>
     * Note: DLQ sending uses non-transactional template for better performance
     * and to avoid transaction overhead on already-failed messages.
     *
     * @param nonTxKafkaTemplate Non-transactional template for sending failed messages to DLQ
     * @return DefaultErrorHandler with configured backoff strategy
     */
    @Bean
    public DefaultErrorHandler errorHandler(
            @org.springframework.beans.factory.annotation.Qualifier("nonTxKafkaTemplate")
            KafkaTemplate<String, Object> nonTxKafkaTemplate) {

        // Configure exponential backoff for retry mechanism
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(backoffInterval, backoffMultiplier);
        exponentialBackOff.setMaxInterval(maxBackoffInterval);
        exponentialBackOff.setMaxElapsedTime(backoffInterval * maxAttempts);

        // Create error handler with backoff strategy
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            // This callback is executed after all retry attempts have been exhausted
            log.error("========================================");
            log.error("Failed to process message after {} retry attempts", maxAttempts);
            log.error("Topic: {}", consumerRecord.topic());
            log.error("Partition: {}", consumerRecord.partition());
            log.error("Offset: {}", consumerRecord.offset());
            log.error("Key: {}", consumerRecord.key());
            log.error("Value: {}", consumerRecord.value());
            log.error("Exception: {}", exception.getMessage(), exception);
            log.error("========================================");

            // Send to Dead Letter Queue (DLQ) if enabled
            if (dlqEnabled) {
                try {
                    String dlqTopic = consumerRecord.topic() + dlqSuffix;
                    log.info("Attempting to send failed message to DLQ topic: {}", dlqTopic);

                    // Use non-transactional template for DLQ - better performance for failed messages
                    nonTxKafkaTemplate.send(dlqTopic, (String) consumerRecord.key(), consumerRecord.value())
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    log.error("Failed to send message to DLQ topic {}: {}", dlqTopic, ex.getMessage(), ex);
                                } else {
                                    log.info("Message successfully sent to DLQ topic {} [partition: {}, offset: {}]",
                                            dlqTopic,
                                            result.getRecordMetadata().partition(),
                                            result.getRecordMetadata().offset());
                                }
                            });
                } catch (Exception e) {
                    log.error("Exception occurred while sending to DLQ: {}", e.getMessage(), e);
                }
            } else {
                log.warn("DLQ is disabled (kafka.dlq.enabled=false). Failed message will NOT be sent to Dead Letter Queue.");
                log.warn("Failed message details - Topic: {}, Key: {}, Value: {}",
                        consumerRecord.topic(), consumerRecord.key(), consumerRecord.value());
            }
        }, exponentialBackOff);

        // Log error handler configuration
        log.info("========================================");
        log.info("Kafka Error Handler Configuration:");
        log.info("Max Retry Attempts: {}", maxAttempts);
        log.info("Initial Backoff Interval: {}ms", backoffInterval);
        log.info("Backoff Multiplier: {}", backoffMultiplier);
        log.info("Max Backoff Interval: {}ms", maxBackoffInterval);
        log.info("----------------------------------------");
        log.info("Dead Letter Queue (DLQ) Configuration:");
        log.info("DLQ Enabled: {}", dlqEnabled);
        log.info("DLQ Topic Suffix: {}", dlqSuffix);
        log.info("DLQ Topic Format: <original-topic>{}", dlqSuffix);
        log.info("DLQ Producer: Non-Transactional (optimized for performance)");
        log.info("========================================");

        return errorHandler;
    }
}

