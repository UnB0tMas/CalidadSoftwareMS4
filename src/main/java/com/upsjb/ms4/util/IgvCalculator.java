// ruta: src/main/java/com/upsjb/ms4/util/IgvCalculator.java
package com.upsjb.ms4.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class IgvCalculator {

    private static final int INTERNAL_PERCENT_SCALE = 8;

    private IgvCalculator() {
    }

    public static IgvBreakdown calcularDesdeBase(BigDecimal baseGravada, BigDecimal igvPorcentaje) {
        BigDecimal base = MoneyUtil.requireNotNegative(baseGravada, "La base gravada");
        BigDecimal porcentaje = normalizarPorcentaje(igvPorcentaje);

        BigDecimal igv = MoneyUtil.money(base.multiply(porcentaje));
        BigDecimal total = MoneyUtil.money(base.add(igv));

        return new IgvBreakdown(
                base,
                MoneyUtil.ZERO,
                MoneyUtil.ZERO,
                igv,
                total,
                MoneyUtil.percent(igvPorcentaje)
        );
    }

    public static IgvBreakdown descomponerDesdeTotal(BigDecimal totalConIgv, BigDecimal igvPorcentaje) {
        BigDecimal total = MoneyUtil.requireNotNegative(totalConIgv, "El total con IGV");
        BigDecimal porcentaje = normalizarPorcentaje(igvPorcentaje);

        BigDecimal divisor = BigDecimal.ONE.add(porcentaje);
        BigDecimal base = total.divide(divisor, MoneyUtil.MONEY_SCALE, MoneyUtil.ROUNDING);
        BigDecimal igv = MoneyUtil.money(total.subtract(base));

        return new IgvBreakdown(
                MoneyUtil.money(base),
                MoneyUtil.ZERO,
                MoneyUtil.ZERO,
                igv,
                total,
                MoneyUtil.percent(igvPorcentaje)
        );
    }

    public static IgvBreakdown calcularOperacionGravada(BigDecimal subtotalFinal, BigDecimal igvPorcentaje) {
        return calcularDesdeBase(subtotalFinal, igvPorcentaje);
    }

    public static BigDecimal calcularIgv(BigDecimal baseGravada, BigDecimal igvPorcentaje) {
        return calcularDesdeBase(baseGravada, igvPorcentaje).igvMonto();
    }

    public static BigDecimal calcularTotalConIgv(BigDecimal baseGravada, BigDecimal igvPorcentaje) {
        return calcularDesdeBase(baseGravada, igvPorcentaje).total();
    }

    private static BigDecimal normalizarPorcentaje(BigDecimal igvPorcentaje) {
        BigDecimal porcentaje = MoneyUtil.requirePercentRange(igvPorcentaje, "El porcentaje de IGV");
        return porcentaje.divide(MoneyUtil.ONE_HUNDRED, INTERNAL_PERCENT_SCALE, RoundingMode.HALF_UP);
    }

    public record IgvBreakdown(
            BigDecimal opGravada,
            BigDecimal opExonerada,
            BigDecimal opInafecta,
            BigDecimal igvMonto,
            BigDecimal total,
            BigDecimal igvPorcentaje
    ) {
    }
}