// ruta: src/main/java/com/upsjb/ms4/shared/exception/BusinessException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BusinessException(String message) {
        this(ErrorCodes.BUSINESS_ERROR, message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = normalizeCode(code);
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
    }

    public BusinessException(String code, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.code = normalizeCode(code);
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    private String normalizeCode(String value) {
        return value == null || value.isBlank() ? ErrorCodes.BUSINESS_ERROR : value.trim();
    }
}