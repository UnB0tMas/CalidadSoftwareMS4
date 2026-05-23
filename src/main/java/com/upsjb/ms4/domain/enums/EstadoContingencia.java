package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoContingencia {

    PENDIENTE_CONFIRMACION("PENDIENTE_CONFIRMACION", "Pendiente de confirmación"),
    ACTIVO("ACTIVO", "Activo"),
    FINALIZADO("FINALIZADO", "Finalizado"),
    CANCELADO("CANCELADO", "Cancelado");

    private final String code;
    private final String label;

    EstadoContingencia(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean activo() {
        return this == ACTIVO;
    }

    public static EstadoContingencia fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de contingencia es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de contingencia no válido: " + code));
    }
}