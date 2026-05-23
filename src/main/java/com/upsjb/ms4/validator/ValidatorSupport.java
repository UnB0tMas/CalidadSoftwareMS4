// ruta: src/main/java/com/upsjb/ms4/validator/ValidatorSupport.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.util.JsonUtil;
import com.upsjb.ms4.util.MoneyUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

abstract class ValidatorSupport {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    protected void require(Object value, String message) {
        if (value == null) {
            fail(message);
        }
    }

    protected void requireText(String value, String message) {
        if (isBlank(value)) {
            fail(message);
        }
    }

    protected void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            fail(fieldName + " debe ser un identificador positivo.");
        }
    }

    protected void requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            fail(fieldName + " debe ser mayor a cero.");
        }
    }

    protected void requirePositive(BigDecimal value, String fieldName) {
        try {
            MoneyUtil.requirePositive(value, fieldName);
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }
    }

    protected void requireNotNegative(BigDecimal value, String fieldName) {
        try {
            MoneyUtil.requireNotNegative(value, fieldName);
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }
    }

    protected void requirePercentRange(BigDecimal value, String fieldName) {
        try {
            MoneyUtil.requirePercentRange(value, fieldName);
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }
    }

    protected void requireEmail(String value, String fieldName) {
        requireText(value, fieldName + " es obligatorio.");

        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            fail(fieldName + " no tiene formato válido.");
        }
    }

    protected void requireMaxLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max) {
            fail(fieldName + " no debe superar " + max + " caracteres.");
        }
    }

    protected void requireJson(String json, String fieldName) {
        requireText(json, fieldName + " es obligatorio.");

        if (!JsonUtil.isValidJson(json)) {
            fail(fieldName + " debe contener JSON válido.");
        }
    }

    protected void requireDateRange(LocalDateTime inicio, LocalDateTime fin, String fieldName) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            fail(fieldName + " no puede tener fecha fin anterior a fecha inicio.");
        }
    }

    protected void requireActive(Boolean estado, String message) {
        if (!Boolean.TRUE.equals(estado)) {
            fail(message);
        }
    }

    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    protected void fail(String message) {
        throw new ValidationException(normalizeMessage(message));
    }

    protected void conflict(String message) {
        throw new ConflictException(normalizeMessage(message));
    }

    private String normalizeMessage(String message) {
        return message == null || message.isBlank()
                ? "La solicitud no cumple las reglas funcionales."
                : message.trim();
    }
}