package com.upsjb.ms4.domain.entity.boleta;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.TipoDescuento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "boleta_detalle")
public class BoletaDetalle extends BaseEntity {

    @Column(name = "id_boleta", nullable = false)
    private Long idBoleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boleta", insertable = false, updatable = false)
    private Boleta boleta;

    @Column(name = "id_venta_detalle", nullable = false, unique = true)
    private Long idVentaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta_detalle", insertable = false, updatable = false)
    private VentaDetalle ventaDetalle;

    @Column(name = "id_producto_ms3", nullable = false)
    private Long idProductoMs3;

    @Column(name = "id_sku_ms3", nullable = false)
    private Long idSkuMs3;

    @Column(name = "codigo_producto", nullable = false, length = 80)
    private String codigoProducto;

    @Column(name = "codigo_sku", nullable = false, length = 80)
    private String codigoSku;

    @Column(name = "nombre_producto", nullable = false, length = 250)
    private String nombreProducto;

    @Column(name = "descripcion_sku", length = 500)
    private String descripcionSku;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario_base", nullable = false, precision = 18, scale = 2)
    private BigDecimal precioUnitarioBase;

    @Column(name = "precio_unitario_final", nullable = false, precision = 18, scale = 2)
    private BigDecimal precioUnitarioFinal;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", length = 30)
    private TipoDescuento tipoDescuento;

    @Column(name = "valor_descuento", precision = 18, scale = 2)
    private BigDecimal valorDescuento;

    @Column(name = "monto_descuento", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoDescuento;

    @Column(name = "id_promocion_ms3")
    private Long idPromocionMs3;

    @Column(name = "id_promocion_version_ms3")
    private Long idPromocionVersionMs3;

    @Column(name = "codigo_promocion", length = 80)
    private String codigoPromocion;

    @Column(name = "igv_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal igvPorcentaje;

    @Column(name = "igv_monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal igvMonto;

    @Column(name = "total_linea", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalLinea;

    @Column(name = "payload_producto_snapshot_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadProductoSnapshotJson;

    @Column(name = "payload_sku_snapshot_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadSkuSnapshotJson;

    @Column(name = "payload_precio_snapshot_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadPrecioSnapshotJson;

    @Column(name = "payload_promocion_snapshot_json", columnDefinition = "nvarchar(max)")
    private String payloadPromocionSnapshotJson;
}