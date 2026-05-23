package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum CanalVenta {

    FISICA("FISICA", "Venta presencial en tienda"),
    ONLINE("ONLINE", "Venta online realizada por cliente autenticado");

    private final String code;
    private final String label;

    CanalVenta(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isFisica() {
        return this == FISICA;
    }

    public boolean isOnline() {
        return this == ONLINE;
    }

    public static CanalVenta fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El canal de venta es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Canal de venta no válido: " + code));
    }
}