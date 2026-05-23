// ruta: src/main/java/com/upsjb/ms4/util/DiscountCalculator.java
package com.upsjb.ms4.util;

import com.upsjb.ms4.domain.enums.TipoDescuento;

import java.math.BigDecimal;

public final class DiscountCalculator {

    private DiscountCalculator() {
    }

    public static DiscountResult aplicar(BigDecimal precioUnitarioBase,
                                         Integer cantidad,
                                         TipoDescuento tipoDescuento,
                                         BigDecimal valorDescuento) {
        BigDecimal precioBase = MoneyUtil.requirePositive(precioUnitarioBase, "El precio unitario base");
        int unidades = requireCantidad(cantidad);

        BigDecimal subtotalBase = MoneyUtil.multiply(precioBase, unidades);
        BigDecimal descuentoUnitario = calcularDescuentoUnitario(precioBase, tipoDescuento, valorDescuento);
        BigDecimal descuentoTotal = MoneyUtil.min(MoneyUtil.multiply(descuentoUnitario, unidades), subtotalBase);
        BigDecimal precioFinalUnitario = MoneyUtil.max(BigDecimal.ZERO, precioBase.subtract(descuentoUnitario));
        BigDecimal subtotalFinal = MoneyUtil.money(subtotalBase.subtract(descuentoTotal));

        return new DiscountResult(
                precioBase,
                precioFinalUnitario,
                subtotalBase,
                descuentoUnitario,
                descuentoTotal,
                subtotalFinal,
                tipoDescuento,
                tipoDescuento == null ? null : MoneyUtil.money(valorDescuento)
        );
    }

    public static BigDecimal calcularDescuentoUnitario(BigDecimal precioUnitarioBase,
                                                       TipoDescuento tipoDescuento,
                                                       BigDecimal valorDescuento) {
        if (tipoDescuento == null || valorDescuento == null || MoneyUtil.isZero(valorDescuento)) {
            return MoneyUtil.ZERO;
        }

        BigDecimal precioBase = MoneyUtil.requirePositive(precioUnitarioBase, "El precio unitario base");
        BigDecimal valor = MoneyUtil.requireNotNegative(valorDescuento, "El valor del descuento");

        BigDecimal descuento = switch (tipoDescuento) {
            case PORCENTAJE -> {
                MoneyUtil.requirePercentRange(valor, "El descuento porcentual");
                yield precioBase.multiply(valor)
                        .divide(MoneyUtil.ONE_HUNDRED, MoneyUtil.MONEY_SCALE, MoneyUtil.ROUNDING);
            }
            case MONTO_FIJO -> valor;
        };

        return MoneyUtil.min(descuento, precioBase);
    }

    public static BigDecimal calcularDescuentoTotal(BigDecimal precioUnitarioBase,
                                                    Integer cantidad,
                                                    TipoDescuento tipoDescuento,
                                                    BigDecimal valorDescuento) {
        return aplicar(precioUnitarioBase, cantidad, tipoDescuento, valorDescuento).descuentoTotal();
    }

    public static BigDecimal calcularPrecioFinalUnitario(BigDecimal precioUnitarioBase,
                                                         TipoDescuento tipoDescuento,
                                                         BigDecimal valorDescuento) {
        BigDecimal precioBase = MoneyUtil.requirePositive(precioUnitarioBase, "El precio unitario base");
        BigDecimal descuento = calcularDescuentoUnitario(precioBase, tipoDescuento, valorDescuento);
        return MoneyUtil.max(BigDecimal.ZERO, precioBase.subtract(descuento));
    }

    private static int requireCantidad(Integer cantidad) {
        int unidades = cantidad == null ? 0 : cantidad;

        if (unidades <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        return unidades;
    }

    public record DiscountResult(
            BigDecimal precioUnitarioBase,
            BigDecimal precioUnitarioFinal,
            BigDecimal subtotalBase,
            BigDecimal descuentoUnitario,
            BigDecimal descuentoTotal,
            BigDecimal subtotalFinal,
            TipoDescuento tipoDescuento,
            BigDecimal valorDescuento
    ) {
    }
}