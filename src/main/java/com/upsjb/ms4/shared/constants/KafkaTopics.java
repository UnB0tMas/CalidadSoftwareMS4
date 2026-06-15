package com.upsjb.ms4.shared.constants;

public final class KafkaTopics {

    public static final String MS2_CLIENTE_SNAPSHOT =
            "ms2.cliente.snapshot.v1";

    public static final String MS2_EMPLEADO_SNAPSHOT =
            "ms2.empleado.snapshot.v1";

    public static final String MS3_PRODUCTO_SNAPSHOT =
            "ms3.producto.snapshot.v2";

    public static final String MS3_PRECIO_SNAPSHOT =
            "ms3.precio.snapshot.v1";

    public static final String MS3_PROMOCION_SNAPSHOT =
            "ms3.promocion.snapshot.v1";

    public static final String MS3_STOCK_SNAPSHOT =
            "ms3.stock.snapshot.v1";

    public static final String MS3_MOVIMIENTO_INVENTARIO =
            "ms3.movimiento-inventario.v1";

    public static final String MS4_STOCK_COMMAND =
            "ms4.stock.command.v1";

    public static final String MS4_STOCK_RECONCILIATION =
            "ms4.stock.reconciliation.v1";

    public static final String MS4_DEAD_LETTER =
            "ms4.dead-letter.v1";

    private KafkaTopics() {
    }
}