package com.upsjb.ms4.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.config.KafkaTopicProperties;
import com.upsjb.ms4.shared.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerErrorHandler.class);
    private static final int MAX_RAW_MESSAGE_LENGTH = 10_000;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;
    private final ObjectMapper objectMapper;

    public KafkaConsumerErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaTopicProperties topicProperties,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
        this.objectMapper = objectMapper;
    }

    public void handle(ConsumerRecord<String, String> record, Exception exception) {
        String topic = record == null ? "UNKNOWN_TOPIC" : safe(record.topic(), "UNKNOWN_TOPIC");
        String key = record == null ? "UNKNOWN_KEY" : safe(record.key(), "UNKNOWN_KEY");

        String errorCode = resolveErrorCode(exception);
        String errorMessage = exception == null || exception.getMessage() == null
                ? "Error consumiendo evento Kafka."
                : exception.getMessage();

        log.error(
                "Error consumiendo evento Kafka en MS4. topic={}, partition={}, offset={}, key={}, errorCode={}, message={}",
                topic,
                record == null ? null : record.partition(),
                record == null ? null : record.offset(),
                key,
                errorCode,
                errorMessage,
                exception
        );

        sendToDeadLetter(topic, key, record, errorCode, errorMessage);
    }

    private void sendToDeadLetter(
            String sourceTopic,
            String sourceKey,
            ConsumerRecord<String, String> record,
            String errorCode,
            String errorMessage
    ) {
        try {
            String payload = buildDeadLetterPayload(sourceTopic, sourceKey, record, errorCode, errorMessage);
            kafkaTemplate.send(topicProperties.deadLetterTopic(), sourceKey, payload);
        } catch (Exception ex) {
            log.error("No se pudo enviar evento fallido a DLQ MS4.", ex);
            throw new IllegalStateException("No se pudo enviar evento fallido a DLQ MS4.", ex);
        }
    }

    private String buildDeadLetterPayload(
            String sourceTopic,
            String sourceKey,
            ConsumerRecord<String, String> record,
            String errorCode,
            String errorMessage
    ) throws JsonProcessingException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceService", "ms-ventas-facturacion");
        payload.put("sourceTopic", sourceTopic);
        payload.put("sourceKey", sourceKey);
        payload.put("sourcePartition", record == null ? null : record.partition());
        payload.put("sourceOffset", record == null ? null : record.offset());
        payload.put("errorCode", safe(errorCode, "KAFKA_CONSUMER_ERROR"));
        payload.put("errorMessage", safe(errorMessage, "Error consumiendo evento Kafka."));
        payload.put("failedAt", LocalDateTime.now());
        payload.put("rawMessage", truncate(record == null ? null : record.value()));

        return objectMapper.writeValueAsString(payload);
    }

    private String resolveErrorCode(Exception exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getCode();
        }

        if (exception instanceof IllegalArgumentException) {
            return "KAFKA_EVENTO_INVALIDO";
        }

        return "KAFKA_CONSUMER_ERROR";
    }

    private String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim()
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ");
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }

        String clean = value.trim();

        if (clean.length() <= MAX_RAW_MESSAGE_LENGTH) {
            return clean;
        }

        return clean.substring(0, MAX_RAW_MESSAGE_LENGTH);
    }
}