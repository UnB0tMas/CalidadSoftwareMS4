// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms2/SnapshotPersonaPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms2;

public record SnapshotPersonaPayloadDto(
        Long idPersona,
        String nombres,
        String apePaterno,
        String apeMaterno,
        String tipoDoc,
        String numeroDoc,
        String correo,
        SnapshotUbigeoPayloadDto distritoDocumento
) {
}