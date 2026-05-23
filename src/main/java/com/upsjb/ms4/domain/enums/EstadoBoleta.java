package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoBoleta {

    EMITIDA("EMITIDA", "Emitida"),
    ANULADA("ANULADA", "Anulada"),
    ERROR_ENVIO_CORREO("ERROR_ENVIO_CORREO", "Error de envío por correo");

    private final String code;
    private final String label;

    EstadoBoleta(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean emitida() {
        return this == EMITIDA;
    }

    public static EstadoBoleta fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de boleta es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de boleta no válido: " + code));
    }
}