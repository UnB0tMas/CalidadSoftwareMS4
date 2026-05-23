package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoCaja {

    ABIERTA("ABIERTA", "Abierta"),
    CERRADA("CERRADA", "Cerrada"),
    ANULADA("ANULADA", "Anulada");

    private final String code;
    private final String label;

    EstadoCaja(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean abierta() {
        return this == ABIERTA;
    }

    public static EstadoCaja fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de caja es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de caja no válido: " + code));
    }
}