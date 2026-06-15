package com.upsjb.ms4.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upsjb.ms4.shared.exception.ValidationException;

import java.util.Map;

public final class JsonUtil {

    private static final JsonInclude.Value NON_NULL_INCLUSION =
            JsonInclude.Value.construct(
                    JsonInclude.Include.NON_NULL,
                    JsonInclude.Include.NON_NULL
            );

    private static final ObjectMapper OBJECT_MAPPER =
            JsonMapper.builder()
                    .addModule(
                            new JavaTimeModule()
                    )
                    .defaultPropertyInclusion(
                            NON_NULL_INCLUSION
                    )
                    .disable(
                            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                    )
                    .disable(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                    )
                    .enable(
                            MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
                    )
                    .enable(
                            SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
                    )
                    .build();

    private JsonUtil() {
        throw new IllegalStateException(
                "JsonUtil es una clase utilitaria y no puede ser instanciada."
        );
    }

    public static String toJson(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(
                    value
            );
        } catch (JsonProcessingException ex) {
            throw new ValidationException(
                    "No se pudo serializar el objeto a JSON."
            );
        }
    }

    public static String toPrettyJson(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(
                            value
                    );
        } catch (JsonProcessingException ex) {
            throw new ValidationException(
                    "No se pudo serializar el objeto a JSON."
            );
        }
    }

    public static String toCanonicalJson(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        try {
            String serialized =
                    OBJECT_MAPPER.writeValueAsString(
                            value
                    );

            Object normalized =
                    OBJECT_MAPPER.readValue(
                            serialized,
                            Object.class
                    );

            return OBJECT_MAPPER.writeValueAsString(
                    normalized
            );
        } catch (JsonProcessingException ex) {
            throw new ValidationException(
                    "No se pudo serializar el objeto a JSON canónico."
            );
        }
    }

    public static <T> T fromJson(
            String json,
            Class<T> targetType
    ) {
        if (
                json == null
                        || json.isBlank()
        ) {
            return null;
        }

        if (targetType == null) {
            throw new ValidationException(
                    "El tipo destino para deserializar el JSON es obligatorio."
            );
        }

        try {
            return OBJECT_MAPPER.readValue(
                    json,
                    targetType
            );
        } catch (JsonProcessingException ex) {
            throw new ValidationException(
                    "El JSON recibido no tiene el formato esperado."
            );
        }
    }

    public static <T> T fromJson(
            String json,
            TypeReference<T> targetType
    ) {
        if (
                json == null
                        || json.isBlank()
        ) {
            return null;
        }

        if (targetType == null) {
            throw new ValidationException(
                    "El tipo destino para deserializar el JSON es obligatorio."
            );
        }

        try {
            return OBJECT_MAPPER.readValue(
                    json,
                    targetType
            );
        } catch (JsonProcessingException ex) {
            throw new ValidationException(
                    "El JSON recibido no tiene el formato esperado."
            );
        }
    }

    public static Map<String, Object> toMap(
            String json
    ) {
        Map<String, Object> result =
                fromJson(
                        json,
                        new TypeReference<Map<String, Object>>() {
                        }
                );

        return result == null
                ? Map.of()
                : result;
    }

    public static JsonNode readTree(
            String json
    ) {
        if (
                json == null
                        || json.isBlank()
        ) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readTree(
                    json
            );
        } catch (JsonProcessingException ex) {
            throw new ValidationException(
                    "El JSON recibido no tiene el formato esperado."
            );
        }
    }

    public static boolean isValidJson(
            String json
    ) {
        if (
                json == null
                        || json.isBlank()
        ) {
            return false;
        }

        try {
            JsonNode node =
                    OBJECT_MAPPER.readTree(
                            json
                    );

            return node != null
                    && !node.isMissingNode();
        } catch (JsonProcessingException ex) {
            return false;
        }
    }
}