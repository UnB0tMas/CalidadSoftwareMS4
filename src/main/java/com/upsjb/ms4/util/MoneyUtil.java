// ruta: src/main/java/com/upsjb/ms4/util/MoneyUtil.java
package com.upsjb.ms4.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public final class MoneyUtil {

    public static final int MONEY_SCALE = 2;
    public static final int PERCENT_SCALE = 4;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
    public static final BigDecimal ONE = BigDecimal.ONE.setScale(MONEY_SCALE, ROUNDING);
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private MoneyUtil() {
    }

    public static BigDecimal money(BigDecimal value) {
        return value == null ? ZERO : value.setScale(MONEY_SCALE, ROUNDING);
    }

    public static BigDecimal percent(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(PERCENT_SCALE, ROUNDING)
                : value.setScale(PERCENT_SCALE, ROUNDING);
    }

    public static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return money(nullToZero(a).add(nullToZero(b)));
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return money(nullToZero(a).subtract(nullToZero(b)));
    }

    public static BigDecimal multiply(BigDecimal amount, Integer quantity) {
        return money(nullToZero(amount).multiply(BigDecimal.valueOf(quantity == null ? 0 : quantity)));
    }

    public static BigDecimal multiply(BigDecimal amount, Long quantity) {
        return money(nullToZero(amount).multiply(BigDecimal.valueOf(quantity == null ? 0L : quantity)));
    }

    public static BigDecimal multiply(BigDecimal amount, BigDecimal factor) {
        return money(nullToZero(amount).multiply(nullToZero(factor)));
    }

    public static BigDecimal divide(BigDecimal amount, BigDecimal divisor) {
        if (isZero(divisor)) {
            throw new IllegalArgumentException("No se puede dividir entre cero.");
        }

        return money(nullToZero(amount).divide(divisor, MONEY_SCALE, ROUNDING));
    }

    public static BigDecimal sum(Collection<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return ZERO;
        }

        return money(values.stream()
                .map(MoneyUtil::nullToZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNotNegative(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    public static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (!isPositive(value)) {
            throw new IllegalArgumentException(resolveFieldName(fieldName) + " debe ser mayor a cero.");
        }

        return money(value);
    }

    public static BigDecimal requireNotNegative(BigDecimal value, String fieldName) {
        if (isNegative(value)) {
            throw new IllegalArgumentException(resolveFieldName(fieldName) + " no puede ser negativo.");
        }

        return money(value);
    }

    public static BigDecimal requirePercentRange(BigDecimal value, String fieldName) {
        BigDecimal percentage = requireNotNegative(value, fieldName);

        if (percentage.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException(resolveFieldName(fieldName) + " no puede superar 100.");
        }

        return percent(percentage);
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return money(nullToZero(a).min(nullToZero(b)));
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return money(nullToZero(a).max(nullToZero(b)));
    }

    public static Long toMinorUnits(BigDecimal amount) {
        BigDecimal value = requireNotNegative(amount, "El monto");
        return value.movePointRight(2).longValueExact();
    }

    public static BigDecimal fromMinorUnits(Long amountMinor) {
        if (amountMinor == null) {
            return ZERO;
        }

        return money(BigDecimal.valueOf(amountMinor).movePointLeft(2));
    }

    private static String resolveFieldName(String fieldName) {
        return fieldName == null || fieldName.isBlank() ? "El monto" : fieldName.trim();
    }
}