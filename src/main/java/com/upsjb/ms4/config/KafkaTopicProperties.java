// ruta: src/main/java/com/upsjb/ms4/config/KafkaTopicProperties.java
package com.upsjb.ms4.config;

import com.upsjb.ms4.shared.constants.KafkaTopics;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ms4.kafka.topics")
public record KafkaTopicProperties(
        String ms2ClienteSnapshot,
        String ms2EmpleadoSnapshot,
        String ms3ProductoSnapshot,
        String ms3PrecioSnapshot,
        String ms3PromocionSnapshot,
        String ms3StockSnapshot,
        String ms3MovimientoInventario,
        String ms4StockCommand,
        String ms4StockReconciliation,
        String deadLetter
) {

    public String clienteSnapshotTopic() {
        return defaultIfBlank(ms2ClienteSnapshot, KafkaTopics.MS2_CLIENTE_SNAPSHOT);
    }

    public String empleadoSnapshotTopic() {
        return defaultIfBlank(ms2EmpleadoSnapshot, KafkaTopics.MS2_EMPLEADO_SNAPSHOT);
    }

    public String productoSnapshotTopic() {
        return defaultIfBlank(ms3ProductoSnapshot, KafkaTopics.MS3_PRODUCTO_SNAPSHOT);
    }

    public String precioSnapshotTopic() {
        return defaultIfBlank(ms3PrecioSnapshot, KafkaTopics.MS3_PRECIO_SNAPSHOT);
    }

    public String promocionSnapshotTopic() {
        return defaultIfBlank(ms3PromocionSnapshot, KafkaTopics.MS3_PROMOCION_SNAPSHOT);
    }

    public String stockSnapshotTopic() {
        return defaultIfBlank(ms3StockSnapshot, KafkaTopics.MS3_STOCK_SNAPSHOT);
    }

    public String movimientoInventarioTopic() {
        return defaultIfBlank(ms3MovimientoInventario, KafkaTopics.MS3_MOVIMIENTO_INVENTARIO);
    }

    public String stockCommandTopic() {
        return defaultIfBlank(ms4StockCommand, KafkaTopics.MS4_STOCK_COMMAND);
    }

    public String stockReconciliationTopic() {
        return defaultIfBlank(ms4StockReconciliation, KafkaTopics.MS4_STOCK_RECONCILIATION);
    }

    public String deadLetterTopic() {
        return defaultIfBlank(deadLetter, "ms4.dead-letter.v1");
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}