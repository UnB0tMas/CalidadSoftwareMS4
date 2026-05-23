// ruta: src/main/java/com/upsjb/ms4/kafka/support/KafkaEnvelopeReader.java
package com.upsjb.ms4.kafka.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaEnvelopeReader {

    private final ObjectMapper objectMapper;

    public KafkaEnvelopeReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> DomainEventEnvelopeDto<T> read(String rawJson, Class<T> payloadType) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new KafkaPublishException("El mensaje Kafka recibido está vacío.");
        }

        if (payloadType == null) {
            throw new KafkaPublishException("El tipo de payload Kafka es obligatorio.");
        }

        try {
            JsonNode root = objectMapper.readTree(rawJson);

            if (root == null || root.isNull() || root.isMissingNode()) {
                throw new KafkaPublishException("El mensaje Kafka recibido no contiene JSON válido.");
            }

            JsonNode envelopeNode = unwrapEnvelope(root);
            JsonNode payloadNode = resolvePayloadNode(envelopeNode);
            T payload = objectMapper.treeToValue(payloadNode, payloadType);

            JsonNode traceNode = envelopeNode.path("trace");
            JsonNode actorNode = envelopeNode.path("actor");

            String producer = firstText(
                    text(envelopeNode, "producer", null),
                    text(envelopeNode, "sourceService", null)
            );

            String sourceService = firstText(
                    text(envelopeNode, "sourceService", null),
                    producer
            );

            String requestId = firstText(
                    text(envelopeNode, "requestId", null),
                    text(traceNode, "requestId", null),
                    text(payloadNode, "requestId", null)
            );

            String correlationId = firstText(
                    text(envelopeNode, "correlationId", null),
                    text(traceNode, "correlationId", null),
                    text(payloadNode, "correlationId", null)
            );

            return new DomainEventEnvelopeDto<>(
                    uuid(envelopeNode, "eventId"),
                    requiredText(envelopeNode, "eventType"),
                    sourceService,
                    requiredText(envelopeNode, "aggregateType"),
                    requiredText(envelopeNode, "aggregateId"),
                    resolveVersion(envelopeNode),
                    producer,
                    parseDateTime(requiredText(envelopeNode, "occurredAt")),
                    requestId,
                    correlationId,
                    payload,
                    metadata(envelopeNode, actorNode, traceNode)
            );
        } catch (KafkaPublishException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new KafkaPublishException("No se pudo deserializar el envelope Kafka recibido.", ex);
        }
    }

    private JsonNode unwrapEnvelope(JsonNode root) {
        if (root.hasNonNull("envelope")) {
            JsonNode envelope = root.get("envelope");

            if (envelope == null || envelope.isNull() || envelope.isMissingNode()) {
                throw new KafkaPublishException("El wrapper Kafka contiene envelope inválido.");
            }

            return envelope;
        }

        return root;
    }

    private JsonNode resolvePayloadNode(JsonNode envelopeNode) {
        if (envelopeNode.hasNonNull("payload")) {
            return envelopeNode.get("payload");
        }

        if (envelopeNode.hasNonNull("data")) {
            return envelopeNode.get("data");
        }

        throw new KafkaPublishException("El evento Kafka no contiene payload ni data.");
    }

    private Integer resolveVersion(JsonNode envelopeNode) {
        if (envelopeNode.hasNonNull("schemaVersion")) {
            return envelopeNode.path("schemaVersion").asInt(1);
        }

        if (envelopeNode.hasNonNull("eventVersion")) {
            return envelopeNode.path("eventVersion").asInt(1);
        }

        if (envelopeNode.hasNonNull("version")) {
            return envelopeNode.path("version").asInt(1);
        }

        return 1;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> metadata(JsonNode envelopeNode, JsonNode actorNode, JsonNode traceNode) {
        Map<String, Object> result = new LinkedHashMap<>();

        JsonNode metadataNode = envelopeNode.path("metadata");
        if (metadataNode != null && !metadataNode.isMissingNode() && !metadataNode.isNull()) {
            result.putAll(objectMapper.convertValue(metadataNode, Map.class));
        }

        if (actorNode != null && !actorNode.isMissingNode() && !actorNode.isNull()) {
            result.put("actor", objectMapper.convertValue(actorNode, Map.class));
        }

        if (traceNode != null && !traceNode.isMissingNode() && !traceNode.isNull()) {
            result.put("trace", objectMapper.convertValue(traceNode, Map.class));
        }

        return Map.copyOf(result);
    }

    private UUID uuid(JsonNode node, String field) {
        String value = requiredText(node, field);

        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new KafkaPublishException("El campo " + field + " no tiene formato UUID válido.");
        }
    }

    private String requiredText(JsonNode node, String field) {
        String value = text(node, field, null);

        if (value == null || value.isBlank()) {
            throw new KafkaPublishException("El campo " + field + " del evento Kafka es obligatorio.");
        }

        return value.trim();
    }

    private String text(JsonNode node, String field, String fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return fallback;
        }

        JsonNode value = node.path(field);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return fallback;
        }

        String raw = value.asText();
        return raw == null || raw.isBlank() ? fallback : raw.trim();
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
            // Intenta formato con offset.
        }

        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception ignored) {
            // Intenta Instant UTC.
        }

        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (Exception ex) {
            throw new KafkaPublishException("El campo occurredAt no tiene formato de fecha válido.");
        }
    }
}