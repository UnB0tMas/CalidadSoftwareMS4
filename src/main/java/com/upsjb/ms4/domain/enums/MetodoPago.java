package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum MetodoPago {

    EFECTIVO("EFECTIVO", "Efectivo"),
    TARJETA_PRESENCIAL_STRIPE_SANDBOX("TARJETA_PRESENCIAL_STRIPE_SANDBOX", "Tarjeta presencial Stripe Sandbox"),
    TARJETA_ONLINE_STRIPE_SANDBOX("TARJETA_ONLINE_STRIPE_SANDBOX", "Tarjeta online Stripe Sandbox");

    private final String code;
    private final String label;

    MetodoPago(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean esEfectivo() {
        return this == EFECTIVO;
    }

    public boolean esStripe() {
        return this == TARJETA_PRESENCIAL_STRIPE_SANDBOX || this == TARJETA_ONLINE_STRIPE_SANDBOX;
    }

    public boolean permitidoParaCanal(CanalVenta canalVenta) {
        if (canalVenta == null) {
            return false;
        }

        if (canalVenta.isOnline()) {
            return this == TARJETA_ONLINE_STRIPE_SANDBOX;
        }

        return this == EFECTIVO || this == TARJETA_PRESENCIAL_STRIPE_SANDBOX;
    }

    public static MetodoPago fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El método de pago es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no válido: " + code));
    }
}