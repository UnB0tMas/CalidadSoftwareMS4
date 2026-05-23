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
@Table(name = "sku_snapshot_ms3")
public class SkuSnapshotMs3 extends BaseEntity {

    @Column(name = "id_sku_ms3", nullable = false)
    private Long idSkuMs3;

    @Column(name = "id_producto_ms3", nullable = false)
    private Long idProductoMs3;

    @Column(name = "codigo_producto", nullable = false, length = 80)
    private String codigoProducto;

    @Column(name = "codigo_sku", nullable = false, length = 80)
    private String codigoSku;

    @Column(name = "barcode", length = 120)
    private String barcode;

    @Column(name = "color", length = 80)
    private String color;

    @Column(name = "talla", length = 80)
    private String talla;

    @Column(name = "material", length = 120)
    private String material;

    @Column(name = "modelo", length = 120)
    private String modelo;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "stock_maximo")
    private Integer stockMaximo;

    @Column(name = "peso_gramos", precision = 18, scale = 2)
    private BigDecimal pesoGramos;

    @Column(name = "alto_cm", precision = 18, scale = 2)
    private BigDecimal altoCm;

    @Column(name = "ancho_cm", precision = 18, scale = 2)
    private BigDecimal anchoCm;

    @Column(name = "largo_cm", precision = 18, scale = 2)
    private BigDecimal largoCm;

    @Column(name = "estado_sku", length = 60)
    private String estadoSku;

    @Column(name = "atributos_json", columnDefinition = "nvarchar(max)")
    private String atributosJson;

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