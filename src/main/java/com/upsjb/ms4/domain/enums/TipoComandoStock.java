// ruta: src/main/java/com/upsjb/ms4/domain/enums/TipoComandoStock.java
package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum TipoComandoStock {

    RESERVAR_STOCK(
            "RESERVAR_STOCK",
            "Reservar stock",
            "VentaStockReservadoPendiente"
    ),
    CONFIRMAR_VENTA(
            "CONFIRMAR_VENTA",
            "Confirmar venta y descontar stock",
            "VentaStockConfirmadoPendiente"
    ),
    LIBERAR_RESERVA(
            "LIBERAR_RESERVA",
            "Liberar reserva",
            "VentaStockLiberadoPendiente"
    ),
    ANULAR_VENTA(
            "ANULAR_VENTA",
            "Anular venta",
            "VentaAnuladaStockPendiente"
    ),
    RECONCILIAR_STOCK(
            "RECONCILIAR_STOCK",
            "Reconciliar stock",
            null
    );

    private final String code;
    private final String label;
    private final String ms3EventType;

    TipoComandoStock(
            String code,
            String label,
            String ms3EventType
    ) {
        this.code = code;
        this.label = label;
        this.ms3EventType = ms3EventType;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String ms3PayloadEventType() {
        return ms3EventType();
    }

    public String ms3EnvelopeEventType() {
        return ms3EventType();
    }

    public String ms3EventType() {
        if (ms3EventType == null) {
            throw new IllegalStateException("El comando " + code + " no está soportado por ms4.stock.command.v1 en MS3.");
        }

        return ms3EventType;
    }

    public boolean soportadoPorMs3StockCommand() {
        return ms3EventType != null;
    }

    public boolean impactaInventario() {
        return this == RESERVAR_STOCK
                || this == CONFIRMAR_VENTA
                || this == LIBERAR_RESERVA
                || this == ANULAR_VENTA
                || this == RECONCILIAR_STOCK;
    }

    public static TipoComandoStock fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El tipo de comando de stock es obligatorio.");
        }

        String normalized = code.trim();

        return Arrays.stream(values())
                .filter(value ->
                        value.code.equalsIgnoreCase(normalized)
                                || value.name().equalsIgnoreCase(normalized)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de comando de stock no válido: " + code));
    }
}