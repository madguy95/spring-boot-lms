package com.springjwt.module.message.business.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Message Producer Service
 * <p>
 * Provides multiple ways to send messages to Kafka topics:
 * - Non-transactional methods (default): sendMessage(), sendMessageSync() - High performance
 * - Transactional methods: sendMessageInTransaction(), sendMessagesInTransaction() - Exactly-once guarantee
 * <p>
 * Choose the appropriate method based on your requirements:
 * - Default (non-transactional): Fast, suitable for most use cases
 * - Transactional: Critical data that requires exactly-once semantics
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaMessageProducer {

    // Non-transactional template (default) - for most use cases
    @Autowired
    @Qualifier("nonTxKafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    // Transactional template - for critical operations
    @Autowired
    @Qualifier("txKafkaTemplate")
    private KafkaTemplate<String, Object> txKafkaTemplate;

    // ===== Non-Transactional Methods (Default - Fast & Simple) =====

    /**
     * Send message to Kafka topic (Async - Non-Transactional)
     * <p>
     * This is the default method for most use cases.
     * Fast and efficient without transaction overhead.
     *
     * @param topic   Topic name
     * @param message Message object
     * @return CompletableFuture with SendResult
     */
    public CompletableFuture<SendResult<String, Object>> sendMessage(String topic, Object message) {
        return sendMessage(topic, null, message);
    }

    /**
     * Send message to Kafka topic with key (Async - Non-Transactional)
     * <p>
     * Messages with the same key go to the same partition (ordering guarantee).
     * Fast performance without transaction overhead.
     *
     * @param topic   Topic name
     * @param key     Message key (for partitioning and ordering)
     * @param message Message object
     * @return CompletableFuture with SendResult
     */
    public CompletableFuture<SendResult<String, Object>> sendMessage(String topic, String key, Object message) {
        log.info("Sending message to topic: {} with key: {}", topic, key);
        log.debug("Message content: {}", message);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send message to topic {}: {}", topic, ex.getMessage(), ex);
            } else {
                log.info("Message sent successfully to topic: {}, partition: {}, offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }

    /**
     * Send message synchronously (Sync - Non-Transactional)
     * <p>
     * Blocks the current thread until message is sent and acknowledged.
     * Use when you need immediate confirmation.
     *
     * @param topic   Topic name
     * @param key     Message key
     * @param message Message object
     * @return SendResult
     * @throws Exception if send fails
     */
    public SendResult<String, Object> sendMessageSync(String topic, String key, Object message) throws Exception {
        log.info("Sending message synchronously to topic: {} with key: {}", topic, key);
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, message).get();
            log.info("Message sent successfully to topic: {}, partition: {}, offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return result;
        } catch (Exception e) {
            log.error("Failed to send message synchronously to topic {}: {}", topic, e.getMessage(), e);
            throw e;
        }
    }

    // ===== Transactional Methods (For Critical Data) =====

    /**
     * Send message with transactional guarantee (Async)
     * <p>
     * Uses transactional producer for exactly-once delivery.
     * Slower than non-transactional but ensures data integrity.
     * <p>
     * Use cases:
     * - Financial transactions
     * - Order processing
     * - Critical data that must not be duplicated or lost
     *
     * @param topic   Topic name
     * @param message Message object
     * @return CompletableFuture with SendResult
     */
    @Transactional
    public CompletableFuture<SendResult<String, Object>> sendMessageInTransaction(String topic, Object message) {
        return sendMessageInTransaction(topic, null, message);
    }

    /**
     * Send message with key and transactional guarantee (Async)
     * <p>
     * Uses transactional producer for exactly-once delivery.
     * Messages with the same key go to the same partition.
     *
     * @param topic   Topic name
     * @param key     Message key (for partitioning and ordering)
     * @param message Message object
     * @return CompletableFuture with SendResult
     */
    @Transactional
    public CompletableFuture<SendResult<String, Object>> sendMessageInTransaction(String topic, String key, Object message) {
        log.info("Sending message to topic: {} with key: {} [Transactional]", topic, key);
        log.debug("Message content: {}", message);

        CompletableFuture<SendResult<String, Object>> future = txKafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send transactional message to topic {}: {}", topic, ex.getMessage(), ex);
            } else {
                log.info("Transactional message sent successfully to topic: {}, partition: {}, offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }

    /**
     * Send message synchronously with transactional guarantee (Sync)
     * <p>
     * Blocks until message is sent with exactly-once guarantee.
     * Use for critical operations that need immediate confirmation.
     *
     * @param topic   Topic name
     * @param key     Message key
     * @param message Message object
     * @return SendResult
     * @throws Exception if send fails
     */
    @Transactional
    public SendResult<String, Object> sendMessageInTransactionSync(String topic, String key, Object message) throws Exception {
        log.info("Sending message synchronously to topic: {} with key: {} [Transactional]", topic, key);
        try {
            SendResult<String, Object> result = txKafkaTemplate.send(topic, key, message).get();
            log.info("Transactional message sent successfully to topic: {}, partition: {}, offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return result;
        } catch (Exception e) {
            log.error("Failed to send transactional message synchronously to topic {}: {}", topic, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send multiple messages in a single transaction
     * <p>
     * All messages are sent atomically - either all succeed or all fail.
     * Perfect for operations that span multiple topics and must be consistent.
     * <p>
     * IMPORTANT: This method uses KafkaTemplate.executeInTransaction() which:
     * - Creates a new transaction
     * - Commits if callback returns true
     * - Rolls back if callback returns false or throws exception
     * <p>
     * Example:
     * <pre>
     * {@code
     * producer.sendMessagesInTransaction(ops -> {
     *     ops.send("orders", order);
     *     ops.send("inventory", inventoryUpdate);
     *     ops.send("notifications", notification);
     *     return true; // Commit all
     * });
     * }
     * </pre>
     *
     * @param callback Operations to execute within transaction
     * @return true if transaction committed successfully
     */
    public boolean sendMessagesInTransaction(TransactionCallback callback) {
        log.info("Starting transactional message batch");
        try {
            Boolean result = txKafkaTemplate.executeInTransaction(operations -> {
                try {
                    boolean success = callback.doInTransaction(operations);
                    if (success) {
                        log.debug("Transaction callback returned true - will commit");
                    } else {
                        log.warn("Transaction callback returned false - will rollback");
                    }
                    return success;
                } catch (Exception e) {
                    log.error("Error in transaction callback: {}", e.getMessage(), e);
                    throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
                }
            });

            if (Boolean.TRUE.equals(result)) {
                log.info("Transactional message batch completed successfully - COMMITTED");
                return true;
            } else {
                log.warn("Transactional message batch completed with false - ROLLED BACK");
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to execute transactional message batch - ROLLED BACK: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send multiple messages in a single transaction (simpler version)
     * <p>
     * Automatically commits if all operations succeed, rolls back on any exception.
     * This is a convenience method that always returns true on success.
     * <p>
     * Example:
     * <pre>
     * {@code
     * boolean success = producer.sendMessagesInTransactionAuto(ops -> {
     *     ops.send("orders", order);
     *     ops.send("inventory", inventoryUpdate);
     *     ops.send("notifications", notification);
     *     // No return needed - auto commit if no exception
     * });
     * }
     * </pre>
     *
     * @param callback Operations to execute within transaction
     * @return true if transaction committed successfully, false if rolled back
     */
    public boolean sendMessagesInTransactionAuto(TransactionCallbackVoid callback) {
        return sendMessagesInTransaction(ops -> {
            callback.doInTransaction(ops);
            return true; // Auto commit
        });
    }

    // ===== Callback Interfaces =====

    /**
     * Callback interface for transactional operations with return value
     */
    @FunctionalInterface
    public interface TransactionCallback {
        /**
         * Execute operations within a transaction
         *
         * @param operations KafkaTemplate to use for sending messages
         * @return true to commit, false to rollback
         * @throws Exception if operation fails (will rollback)
         */
        boolean doInTransaction(KafkaOperations<String, Object> operations) throws Exception;
    }

    /**
     * Callback interface for transactional operations without return value
     * (auto-commit on success, rollback on exception)
     */
    @FunctionalInterface
    public interface TransactionCallbackVoid {
        /**
         * Execute operations within a transaction
         *
         * @param operations KafkaTemplate to use for sending messages
         * @throws Exception if operation fails (will rollback)
         */
        void doInTransaction(KafkaOperations<String, Object> operations) throws Exception;
    }
}

