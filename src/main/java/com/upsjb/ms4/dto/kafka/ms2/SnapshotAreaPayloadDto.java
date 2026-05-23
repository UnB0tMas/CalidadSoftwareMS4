// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms2/SnapshotAreaPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms2;

public record SnapshotAreaPayloadDto(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String display,
        Boolean estado
) {
}