// ruta: src/main/java/com/upsjb/ms4/service/contract/venta/VentaCalculoResultado.java
package com.upsjb.ms4.service.contract.venta;

import java.math.BigDecimal;
import java.util.List;

public record VentaCalculoResultado(
        String moneda,
        BigDecimal subtotal,
        BigDecimal descuentoTotal,
        BigDecimal opGravada,
        BigDecimal opExonerada,
        BigDecimal opInafecta,
        BigDecimal igvPorcentaje,
        BigDecimal igvTotal,
        BigDecimal total,
        Boolean stockSuficiente,
        List<VentaLineaCalculada> lineas,
        List<String> advertencias
) {
}