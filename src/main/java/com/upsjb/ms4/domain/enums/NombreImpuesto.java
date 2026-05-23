package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum NombreImpuesto {

    IGV("IGV", "Impuesto General a las Ventas");

    private final String code;
    private final String label;

    NombreImpuesto(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static NombreImpuesto fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El impuesto es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Impuesto no válido: " + code));
    }
}