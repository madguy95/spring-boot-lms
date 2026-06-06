package com.springjwt.module.message.business.consumer;

import com.springjwt.core.kafka.KafkaTopics;
import com.springjwt.module.message.model.dto.EmailData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka Message Consumer Service
 * Handles consuming messages from Kafka topics with manual acknowledgment
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaMessageConsumer {

    /**
     * Consume email queue messages from Kafka
     */
    @KafkaListener(
            topics = KafkaTopics.EMAIL_QUEUE,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "emailListenerContainerFactory"
    )
    public void consumeEmailQueue(@Payload EmailData emailData,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received email from topic={}, partition={}, offset={}", topic, partition, offset);
            log.debug("EmailData: {}", emailData);

            // TODO: Implement email sending logic

            acknowledgment.acknowledge();
            log.debug("Acknowledged offset={}", offset);

        } catch (Exception e) {
            log.error("Error processing email request: {}", e.getMessage(), e);
            // Don't acknowledge -> message will be retried
        }
    }
}
