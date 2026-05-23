// ruta: src/main/java/com/upsjb/ms4/dto/boleta/response/BoletaPreviewResponseDto.java
package com.upsjb.ms4.dto.boleta.response;

public record BoletaPreviewResponseDto(
        Long idBoleta,
        String codigoBoleta,
        String html,
        String versionPlantilla
) {
}