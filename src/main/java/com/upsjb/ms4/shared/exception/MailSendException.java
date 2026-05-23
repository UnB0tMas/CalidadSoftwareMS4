// ruta: src/main/java/com/upsjb/ms4/shared/exception/MailSendException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class MailSendException extends BusinessException {

    public MailSendException(String message) {
        super(ErrorCodes.MAIL_SEND_ERROR, message, HttpStatus.BAD_GATEWAY);
    }

    public MailSendException(String message, Throwable cause) {
        super(ErrorCodes.MAIL_SEND_ERROR, message, HttpStatus.BAD_GATEWAY, cause);
    }
}