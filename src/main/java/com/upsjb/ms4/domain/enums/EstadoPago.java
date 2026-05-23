package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoPago {

    PENDIENTE("PENDIENTE", "Pendiente"),
    APROBADO("APROBADO", "Aprobado"),
    RECHAZADO("RECHAZADO", "Rechazado"),
    ANULADO("ANULADO", "Anulado"),
    REEMBOLSADO("REEMBOLSADO", "Reembolsado"),
    ERROR("ERROR", "Error");

    private final String code;
    private final String label;

    EstadoPago(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean aprobado() {
        return this == APROBADO;
    }

    public boolean finalizado() {
        return this == APROBADO || this == RECHAZADO || this == ANULADO || this == REEMBOLSADO;
    }

    public static EstadoPago fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de pago es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de pago no válido: " + code));
    }
}