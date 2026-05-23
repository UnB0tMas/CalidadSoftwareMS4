package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum TipoMovimientoCaja {

    APERTURA("APERTURA", "Apertura de caja"),
    VENTA_EFECTIVO("VENTA_EFECTIVO", "Venta en efectivo"),
    VENTA_TARJETA("VENTA_TARJETA", "Venta con tarjeta"),
    CIERRE("CIERRE", "Cierre de caja"),
    AJUSTE("AJUSTE", "Ajuste manual de caja"),
    ANULACION("ANULACION", "Anulación asociada a caja");

    private final String code;
    private final String label;

    TipoMovimientoCaja(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TipoMovimientoCaja fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de movimiento de caja es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de movimiento de caja no válido: " + code));
    }
}