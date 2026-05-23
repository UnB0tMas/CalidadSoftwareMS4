// ruta: src/main/java/com/upsjb/ms4/service/contract/venta/VentaLineaCalculada.java
package com.upsjb.ms4.service.contract.venta;

import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.domain.enums.TipoDescuento;

import java.math.BigDecimal;
import java.util.Optional;

public record VentaLineaCalculada(
        ProductoSnapshotMs3 producto,
        SkuSnapshotMs3 sku,
        PrecioSnapshotMs3 precio,
        Optional<PromocionSnapshotMs3> promocion,
        Optional<PromocionSkuDescuentoSnapshotMs3> descuentoPromocion,
        StockSnapshotMs3 stock,
        Integer cantidad,
        BigDecimal precioUnitarioBase,
        BigDecimal precioUnitarioFinal,
        BigDecimal subtotal,
        TipoDescuento tipoDescuento,
        BigDecimal valorDescuento,
        BigDecimal montoDescuento,
        BigDecimal igvPorcentaje,
        BigDecimal igvMonto,
        BigDecimal totalLinea,
        Boolean stockSuficiente
) {
}