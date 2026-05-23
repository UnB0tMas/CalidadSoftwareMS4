// ruta: src/main/java/com/upsjb/ms4/shared/exception/ForbiddenException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(ErrorCodes.FORBIDDEN, message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(ErrorCodes.FORBIDDEN, message, HttpStatus.FORBIDDEN, cause);
    }
}