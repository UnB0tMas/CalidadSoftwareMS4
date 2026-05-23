// ruta: src/main/java/com/upsjb/ms4/shared/exception/ExternalServiceException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(String message) {
        super(ErrorCodes.EXTERNAL_SERVICE_ERROR, message, HttpStatus.BAD_GATEWAY);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(ErrorCodes.EXTERNAL_SERVICE_ERROR, message, HttpStatus.BAD_GATEWAY, cause);
    }
}