package com.upsjb.ms4.domain.entity.snapshot;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "precio_snapshot_ms3")
public class PrecioSnapshotMs3 extends BaseEntity {

    @Column(name = "id_precio_historial_ms3", nullable = false)
    private Long idPrecioHistorialMs3;

    @Column(name = "id_sku_ms3", nullable = false)
    private Long idSkuMs3;

    @Column(name = "codigo_sku", nullable = false, length = 80)
    private String codigoSku;

    @Column(name = "id_producto_ms3", nullable = false)
    private Long idProductoMs3;

    @Column(name = "codigo_producto", nullable = false, length = 80)
    private String codigoProducto;

    @Column(name = "nombre_producto", nullable = false, length = 250)
    private String nombreProducto;

    @Column(name = "precio_venta", nullable = false, precision = 18, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda;

    @Column(name = "simbolo_moneda", length = 10)
    private String simboloMoneda;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "creado_por_id_usuario_ms1")
    private Long creadoPorIdUsuarioMs1;

    @Column(name = "event_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 120)
    private String aggregateId;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;

    @Column(name = "fecha_sincronizacion", nullable = false)
    private LocalDateTime fechaSincronizacion;
}