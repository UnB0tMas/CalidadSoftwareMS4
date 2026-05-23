// ruta: src/main/java/com/upsjb/ms4/shared/exception/UnauthorizedException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ErrorCodes.UNAUTHORIZED, message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(ErrorCodes.UNAUTHORIZED, message, HttpStatus.UNAUTHORIZED, cause);
    }
}