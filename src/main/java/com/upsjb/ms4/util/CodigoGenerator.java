// ruta: src/main/java/com/upsjb/ms4/util/CodigoGenerator.java
package com.upsjb.ms4.util;

import java.time.LocalDate;
import java.util.Locale;

public final class CodigoGenerator {

    private CodigoGenerator() {
    }

    public static String venta(Long sequence) {
        return correlativoAnual("VEN", sequence);
    }

    public static String pago(Long sequence) {
        return correlativoAnual("PAG", sequence);
    }

    public static String caja(Long sequence) {
        return caja(sequence, DateTimeUtil.today());
    }

    public static String caja(Long sequence, LocalDate fechaOperacion) {
        LocalDate fecha = fechaOperacion == null ? DateTimeUtil.today() : fechaOperacion;
        return "CAJ-" + DateTimeUtil.formatCompactDate(fecha) + "-" + pad(sequence, 4);
    }

    public static String configuracionEmpresaVersion(Long sequence) {
        return configuracionVersion("EMPRESA", sequence);
    }

    public static String configuracionTributariaVersion(Long sequence) {
        return configuracionVersion("IGV", sequence);
    }

    public static String boletaPlantillaVersion(Long sequence) {
        return configuracionVersion("TPL-BOLETA", sequence);
    }

    public static String configuracionVersion(String prefix, Long sequence) {
        return sanitize(prefix) + "-V" + pad(sequence, 6);
    }

    public static String boleta(String serie, Long numero) {
        if (serie == null || serie.isBlank()) {
            throw new IllegalArgumentException("La serie de boleta es obligatoria.");
        }

        return serie.trim().toUpperCase(Locale.ROOT) + "-" + pad(numero, 8);
    }

    public static String correlativoAnual(String prefix, Long sequence) {
        return correlativoAnual(prefix, sequence, DateTimeUtil.today());
    }

    public static String correlativoAnual(String prefix, Long sequence, LocalDate date) {
        LocalDate safeDate = date == null ? DateTimeUtil.today() : date;
        return sanitize(prefix) + "-" + DateTimeUtil.formatYear(safeDate) + "-" + pad(sequence, 6);
    }

    public static String generic(String prefix, Long sequence, int padSize) {
        return sanitize(prefix) + "-" + pad(sequence, padSize);
    }

    public static String pad(Long value, int size) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("El correlativo debe ser mayor o igual a cero.");
        }

        if (size <= 0 || size > 20) {
            throw new IllegalArgumentException("El tamaño de relleno del código debe estar entre 1 y 20.");
        }

        return String.format("%0" + size + "d", value);
    }

    public static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor a normalizar para código es obligatorio.");
        }

        String sanitized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9_-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("El valor normalizado para código no puede quedar vacío.");
        }

        return sanitized;
    }
}