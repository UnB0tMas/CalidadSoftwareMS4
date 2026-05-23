// ruta: src/main/java/com/upsjb/ms4/dto/caja/response/CajaDetailResponseDto.java
package com.upsjb.ms4.dto.caja.response;

import java.util.List;

public record CajaDetailResponseDto(
        CajaResponseDto caja,
        List<CajaMovimientoResponseDto> movimientos
) {
}