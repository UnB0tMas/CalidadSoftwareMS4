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
@Table(name = "stock_snapshot_ms3")
public class StockSnapshotMs3 extends BaseEntity {

    @Column(name = "id_stock_ms3", nullable = false)
    private Long idStockMs3;

    @Column(name = "id_sku_ms3", nullable = false)
    private Long idSkuMs3;

    @Column(name = "codigo_sku", nullable = false, length = 80)
    private String codigoSku;

    @Column(name = "barcode", length = 120)
    private String barcode;

    @Column(name = "id_producto_ms3", nullable = false)
    private Long idProductoMs3;

    @Column(name = "codigo_producto", nullable = false, length = 80)
    private String codigoProducto;

    @Column(name = "nombre_producto", nullable = false, length = 250)
    private String nombreProducto;

    @Column(name = "id_almacen_ms3", nullable = false)
    private Long idAlmacenMs3;

    @Column(name = "codigo_almacen", nullable = false, length = 80)
    private String codigoAlmacen;

    @Column(name = "nombre_almacen", nullable = false, length = 180)
    private String nombreAlmacen;

    @Column(name = "stock_fisico", nullable = false)
    private Integer stockFisico;

    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "stock_maximo")
    private Integer stockMaximo;

    @Column(name = "costo_promedio_actual", precision = 18, scale = 2)
    private BigDecimal costoPromedioActual;

    @Column(name = "ultimo_costo_compra", precision = 18, scale = 2)
    private BigDecimal ultimoCostoCompra;

    @Column(name = "bajo_stock")
    private Boolean bajoStock;

    @Column(name = "sobre_stock")
    private Boolean sobreStock;

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