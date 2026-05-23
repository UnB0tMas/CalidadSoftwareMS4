package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoVenta {

    BORRADOR("BORRADOR", "Venta en borrador"),
    PENDIENTE_PAGO("PENDIENTE_PAGO", "Venta pendiente de pago"),
    PAGADA("PAGADA", "Venta pagada"),
    CONFIRMADA("CONFIRMADA", "Venta confirmada"),
    ANULADA("ANULADA", "Venta anulada"),
    RECHAZADA("RECHAZADA", "Venta rechazada"),
    ERROR_STOCK("ERROR_STOCK", "Venta con error de stock"),
    PENDIENTE_SYNC_STOCK("PENDIENTE_SYNC_STOCK", "Venta pendiente de sincronización de stock");

    private final String code;
    private final String label;

    EstadoVenta(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean permitePago() {
        return this == PENDIENTE_PAGO;
    }

    public boolean esFinalizada() {
        return this == CONFIRMADA || this == ANULADA || this == RECHAZADA;
    }

    public static EstadoVenta fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de venta es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de venta no válido: " + code));
    }
}