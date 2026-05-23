package com.upsjb.ms4.domain.entity.snapshot;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.enums.TipoDescuento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "promocion_sku_descuento_snapshot_ms3")
public class PromocionSkuDescuentoSnapshotMs3 extends BaseEntity {

    @Column(name = "id_promocion_sku_descuento_version_ms3", nullable = false)
    private Long idPromocionSkuDescuentoVersionMs3;

    @Column(name = "id_promocion_version_ms3", nullable = false)
    private Long idPromocionVersionMs3;

    @Column(name = "id_promocion_ms3", nullable = false)
    private Long idPromocionMs3;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false, length = 30)
    private TipoDescuento tipoDescuento;

    @Column(name = "valor_descuento", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorDescuento;

    @Column(name = "precio_final_estimado", precision = 18, scale = 2)
    private BigDecimal precioFinalEstimado;

    @Column(name = "margen_estimado", precision = 18, scale = 2)
    private BigDecimal margenEstimado;

    @Column(name = "limite_unidades")
    private Integer limiteUnidades;

    @Column(name = "prioridad")
    private Integer prioridad;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;
}