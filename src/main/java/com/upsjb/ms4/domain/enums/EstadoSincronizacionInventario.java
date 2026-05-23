package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoSincronizacionInventario {

    PENDIENTE("PENDIENTE", "Pendiente"),
    ENVIADO("ENVIADO", "Enviado"),
    SINCRONIZADO("SINCRONIZADO", "Sincronizado"),
    ERROR("ERROR", "Error"),
    REQUIERE_REVISION("REQUIERE_REVISION", "Requiere revisión");

    private final String code;
    private final String label;

    EstadoSincronizacionInventario(String code, String label) {
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
        return this == PENDIENTE || this == ERROR || this == REQUIERE_REVISION;
    }

    public static EstadoSincronizacionInventario fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de sincronización de inventario es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de sincronización de inventario no válido: " + code));
    }
}