// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms2/SnapshotDireccionPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms2;

public record SnapshotDireccionPayloadDto(
        Long idDireccion,
        String tipoDireccion,
        String direccion,
        String referencia,
        String descripcion,
        Object ubicacion,
        SnapshotUbigeoPayloadDto ubigeo,
        Boolean esPrincipal,
        Boolean estado
) {
}