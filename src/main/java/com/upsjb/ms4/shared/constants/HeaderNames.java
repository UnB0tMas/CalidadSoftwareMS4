// ruta: src/main/java/com/upsjb/ms4/shared/constants/HeaderNames.java
package com.upsjb.ms4.shared.constants;

public final class HeaderNames {

    private HeaderNames() {
    }

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String INTERNAL_SERVICE_KEY = "X-Internal-Service-Key";

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String REQUEST_ID_ALT = "X-Request-ID";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String CORRELATION_ID_ALT = "X-Correlation-ID";

    public static final String USER_AGENT = "User-Agent";
    public static final String FORWARDED = "Forwarded";
    public static final String FORWARDED_FOR = "X-Forwarded-For";
    public static final String FORWARDED_HOST = "X-Forwarded-Host";
    public static final String FORWARDED_PROTO = "X-Forwarded-Proto";
    public static final String FORWARDED_PREFIX = "X-Forwarded-Prefix";
    public static final String REAL_IP = "X-Real-IP";

    public static final String STRIPE_SIGNATURE = "Stripe-Signature";
}