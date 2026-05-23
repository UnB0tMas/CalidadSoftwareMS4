package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum TipoDescuento {

    PORCENTAJE("PORCENTAJE", "Descuento porcentual"),
    MONTO_FIJO("MONTO_FIJO", "Descuento por monto fijo");

    private final String code;
    private final String label;

    TipoDescuento(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TipoDescuento fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de descuento es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de descuento no válido: " + code));
    }
}