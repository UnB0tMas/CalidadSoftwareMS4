// ruta: src/main/java/com/upsjb/ms4/shared/exception/ValidationException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class ValidationException extends BusinessException {

    private final Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        this(message, Map.of());
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(ErrorCodes.VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
    }

    public static ValidationException field(String field, String message) {
        String key = field == null || field.isBlank() ? "request" : field.trim();
        return new ValidationException(message, Map.of(key, List.of(message)));
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}