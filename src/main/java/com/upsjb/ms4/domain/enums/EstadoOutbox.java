package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoOutbox {

    PENDIENTE("PENDIENTE", "Pendiente"),
    PUBLICANDO("PUBLICANDO", "Publicando"),
    PUBLICADO("PUBLICADO", "Publicado"),
    ERROR("ERROR", "Error"),
    DESCARTADO("DESCARTADO", "Descartado");

    private final String code;
    private final String label;

    EstadoOutbox(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean reintentable() {
        return this == PENDIENTE || this == ERROR;
    }

    public static EstadoOutbox fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de outbox es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de outbox no válido: " + code));
    }
}