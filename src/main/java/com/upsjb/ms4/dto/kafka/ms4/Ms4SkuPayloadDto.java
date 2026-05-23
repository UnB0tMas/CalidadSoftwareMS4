// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms4/Ms4SkuPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms4;

public record Ms4SkuPayloadDto(
        Long id,
        String codigo,
        String codigoSku,
        String barcode
) {
}