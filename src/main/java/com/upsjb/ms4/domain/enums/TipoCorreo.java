package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum TipoCorreo {

    BOLETA_COMPRA_FISICA("BOLETA_COMPRA_FISICA", "Boleta por compra física"),
    BOLETA_COMPRA_ONLINE("BOLETA_COMPRA_ONLINE", "Boleta por compra online"),
    ALERTA_MS3_CAIDO("ALERTA_MS3_CAIDO", "Alerta de MS3 caído"),
    ALERTA_MS3_RECUPERADO("ALERTA_MS3_RECUPERADO", "Alerta de MS3 recuperado"),
    ALERTA_CONTINGENCIA_ACTIVADA("ALERTA_CONTINGENCIA_ACTIVADA", "Alerta de contingencia activada"),
    ALERTA_CONTINGENCIA_FINALIZADA("ALERTA_CONTINGENCIA_FINALIZADA", "Alerta de contingencia finalizada"),
    ALERTA_KAFKA_ERROR("ALERTA_KAFKA_ERROR", "Alerta de error Kafka"),
    ALERTA_STRIPE_ERROR("ALERTA_STRIPE_ERROR", "Alerta de error Stripe"),
    ALERTA_CAJA_DIFERENCIA("ALERTA_CAJA_DIFERENCIA", "Alerta de diferencia de caja");

    private final String code;
    private final String label;

    TipoCorreo(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean esBoleta() {
        return this == BOLETA_COMPRA_FISICA || this == BOLETA_COMPRA_ONLINE;
    }

    public static TipoCorreo fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de correo es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de correo no válido: " + code));
    }
}