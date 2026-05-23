// ruta: src/main/java/com/upsjb/ms4/dto/boleta/response/BoletaDetailResponseDto.java
package com.upsjb.ms4.dto.boleta.response;

import java.util.List;

public record BoletaDetailResponseDto(
        BoletaResponseDto boleta,
        List<BoletaDetalleResponseDto> detalles
) {
}