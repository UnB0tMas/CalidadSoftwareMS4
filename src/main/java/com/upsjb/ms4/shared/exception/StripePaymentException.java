// ruta: src/main/java/com/upsjb/ms4/shared/exception/StripePaymentException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class StripePaymentException extends BusinessException {

    public StripePaymentException(String message) {
        super(ErrorCodes.STRIPE_PAYMENT_ERROR, message, HttpStatus.BAD_GATEWAY);
    }

    public StripePaymentException(String message, Throwable cause) {
        super(ErrorCodes.STRIPE_PAYMENT_ERROR, message, HttpStatus.BAD_GATEWAY, cause);
    }
}