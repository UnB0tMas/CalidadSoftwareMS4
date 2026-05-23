// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms2/SnapshotUbigeoPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms2;

public record SnapshotUbigeoPayloadDto(
        Long idDistrito,
        String distrito,
        Long idProvincia,
        String provincia,
        Long idDepartamento,
        String departamento,
        String display
) {
}