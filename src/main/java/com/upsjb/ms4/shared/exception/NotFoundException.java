// ruta: src/main/java/com/upsjb/ms4/shared/exception/NotFoundException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(ErrorCodes.NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message, Throwable cause) {
        super(ErrorCodes.NOT_FOUND, message, HttpStatus.NOT_FOUND, cause);
    }

    public static NotFoundException byId(String resource, Long id) {
        return new NotFoundException(resourceName(resource) + " no encontrado con id: " + id);
    }

    public static NotFoundException inactive(String resource, Long id) {
        return new NotFoundException(resourceName(resource) + " no se encuentra activo con id: " + id);
    }

    private static String resourceName(String resource) {
        return resource == null || resource.isBlank() ? "Recurso" : resource.trim();
    }
}