// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms4/Ms4AlmacenPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms4;

public record Ms4AlmacenPayloadDto(
        Long id,
        String codigo,
        String codigoAlmacen,
        String nombre
) {
}