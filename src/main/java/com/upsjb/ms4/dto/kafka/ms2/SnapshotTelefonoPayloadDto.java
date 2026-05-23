// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms2/SnapshotTelefonoPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms2;

public record SnapshotTelefonoPayloadDto(
        Long idTelefono,
        String tipoTelefono,
        String numero,
        Boolean esPrincipal,
        Boolean estado
) {
}