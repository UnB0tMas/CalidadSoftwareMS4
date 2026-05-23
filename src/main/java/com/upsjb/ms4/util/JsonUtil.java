// ruta: src/main/java/com/upsjb/ms4/util/JsonUtil.java
package com.upsjb.ms4.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upsjb.ms4.shared.exception.ValidationException;

import java.util.Map;

public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

    private JsonUtil() {
    }

    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ValidationException("No se pudo serializar el objeto a JSON.");
        }
    }

    public static String toPrettyJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ValidationException("No se pudo serializar el objeto a JSON.");
        }
    }

    public static String toCanonicalJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            Object normalized = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(value), Object.class);
            return OBJECT_MAPPER.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            throw new ValidationException("No se pudo serializar el objeto a JSON canónico.");
        }
    }

    public static <T> T fromJson(String json, Class<T> targetType) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, targetType);
        } catch (JsonProcessingException ex) {
            throw new ValidationException("El JSON recibido no tiene el formato esperado.");
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> targetType) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, targetType);
        } catch (JsonProcessingException ex) {
            throw new ValidationException("El JSON recibido no tiene el formato esperado.");
        }
    }

    public static Map<String, Object> toMap(String json) {
        Map<String, Object> result = fromJson(json, new TypeReference<>() {
        });

        return result == null ? Map.of() : result;
    }

    public static JsonNode readTree(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new ValidationException("El JSON recibido no tiene el formato esperado.");
        }
    }

    public static boolean isValidJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException ex) {
            return false;
        }
    }
}