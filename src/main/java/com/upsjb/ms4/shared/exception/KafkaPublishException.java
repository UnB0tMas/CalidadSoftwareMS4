// ruta: src/main/java/com/upsjb/ms4/shared/exception/KafkaPublishException.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class KafkaPublishException extends BusinessException {

    public KafkaPublishException(String message) {
        super(ErrorCodes.KAFKA_PUBLISH_ERROR, message, HttpStatus.BAD_GATEWAY);
    }

    public KafkaPublishException(String message, Throwable cause) {
        super(ErrorCodes.KAFKA_PUBLISH_ERROR, message, HttpStatus.BAD_GATEWAY, cause);
    }
}