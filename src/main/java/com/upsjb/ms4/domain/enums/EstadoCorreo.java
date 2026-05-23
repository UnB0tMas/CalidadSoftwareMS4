package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoCorreo {

    PENDIENTE("PENDIENTE", "Pendiente"),
    ENVIANDO("ENVIANDO", "Enviando"),
    ENVIADO("ENVIADO", "Enviado"),
    ERROR("ERROR", "Error"),
    DESCARTADO("DESCARTADO", "Descartado");

    private final String code;
    private final String label;

    EstadoCorreo(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean procesable() {
        return this == PENDIENTE || this == ERROR;
    }

    public static EstadoCorreo fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de correo es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de correo no válido: " + code));
    }
}