// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/response/ContingenciaReconciliacionResponseDto.java
package com.upsjb.ms4.dto.contingencia.response;

public record ContingenciaReconciliacionResponseDto(
        long totalEventosReintentables,
        long totalEventosReprogramados,
        long totalEventosPendientesPosteriores,
        String mensaje
) {
}