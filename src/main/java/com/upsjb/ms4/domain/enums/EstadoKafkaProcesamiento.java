package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum EstadoKafkaProcesamiento {

    RECIBIDO("RECIBIDO", "Recibido"),
    PROCESADO("PROCESADO", "Procesado"),
    IGNORADO("IGNORADO", "Ignorado"),
    ERROR("ERROR", "Error");

    private final String code;
    private final String label;

    EstadoKafkaProcesamiento(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static EstadoKafkaProcesamiento fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El estado de procesamiento Kafka es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de procesamiento Kafka no válido: " + code));
    }
}