// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms2/SnapshotEmpresaPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms2;

public record SnapshotEmpresaPayloadDto(
        Long idEmpresa,
        String ruc,
        String razonSocial,
        String nombreComercial,
        String correo,
        SnapshotUbigeoPayloadDto distritoRegistro
) {
}