// ruta: src/main/java/com/upsjb/ms4/shared/exception/ConflictException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(ErrorCodes.CONFLICT, message, HttpStatus.CONFLICT);
    }

    public ConflictException(String message, Throwable cause) {
        super(ErrorCodes.CONFLICT, message, HttpStatus.CONFLICT, cause);
    }
}