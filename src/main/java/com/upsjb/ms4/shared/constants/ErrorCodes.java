// ruta: src/main/java/com/upsjb/ms4/shared/constants/ErrorCodes.java
package com.upsjb.ms4.shared.constants;

public final class ErrorCodes {

    private ErrorCodes() {
    }

    public static final String OK = "OK";
    public static final String CREATED = "CREATED";
    public static final String ACCEPTED = "ACCEPTED";

    public static final String VALIDATION_ERROR = "MS4_VALIDATION_ERROR";
    public static final String BUSINESS_ERROR = "MS4_BUSINESS_ERROR";
    public static final String NOT_FOUND = "MS4_NOT_FOUND";
    public static final String CONFLICT = "MS4_CONFLICT";
    public static final String UNAUTHORIZED = "MS4_UNAUTHORIZED";
    public static final String FORBIDDEN = "MS4_FORBIDDEN";
    public static final String EXTERNAL_SERVICE_ERROR = "MS4_EXTERNAL_SERVICE_ERROR";
    public static final String KAFKA_PUBLISH_ERROR = "MS4_KAFKA_PUBLISH_ERROR";
    public static final String STRIPE_PAYMENT_ERROR = "MS4_STRIPE_PAYMENT_ERROR";
    public static final String MAIL_SEND_ERROR = "MS4_MAIL_SEND_ERROR";
    public static final String INTERNAL_ERROR = "MS4_INTERNAL_ERROR";
    public static final String INVALID_REQUEST = "MS4_INVALID_REQUEST";
    public static final String INVALID_BODY = "MS4_INVALID_BODY";
    public static final String INVALID_PARAMETER = "MS4_INVALID_PARAMETER";
    public static final String METHOD_NOT_ALLOWED = "MS4_METHOD_NOT_ALLOWED";
    public static final String UNSUPPORTED_MEDIA_TYPE = "MS4_UNSUPPORTED_MEDIA_TYPE";
}