// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms4/Ms4VentaPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms4;

import com.upsjb.ms4.domain.enums.CanalVenta;

public record Ms4VentaPayloadDto(
        Long idVentaMs4,
        String codigoVenta,
        CanalVenta canalVenta
) {
}