// ruta: src/main/java/com/upsjb/ms4/domain/enums/ResultadoAuditoria.java
package com.upsjb.ms4.domain.enums;

import java.util.Arrays;

public enum ResultadoAuditoria {

    EXITOSO("EXITOSO", "Operación exitosa"),
    ERROR_USUARIO("ERROR_USUARIO", "Error funcional o de validación del usuario"),
    ERROR_TECNICO("ERROR_TECNICO", "Error técnico interno o de dependencia externa");

    private final String code;
    private final String label;

    ResultadoAuditoria(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean exitoso() {
        return this == EXITOSO;
    }

    public boolean errorUsuario() {
        return this == ERROR_USUARIO;
    }

    public boolean errorTecnico() {
        return this == ERROR_TECNICO;
    }

    public static ResultadoAuditoria fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El resultado de auditoría es obligatorio.");
        }

        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resultado de auditoría no válido: " + code));
    }
}