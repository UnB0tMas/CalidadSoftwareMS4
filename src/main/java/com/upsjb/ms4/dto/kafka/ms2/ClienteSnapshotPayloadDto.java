// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms2/ClienteSnapshotPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms2;

import java.util.ArrayList;
import java.util.List;

public record ClienteSnapshotPayloadDto(
        Long idCliente,
        Long idUsuarioMs1,
        String tipoCliente,
        Boolean estado,
        SnapshotPersonaPayloadDto persona,
        SnapshotEmpresaPayloadDto empresa,

        List<SnapshotTelefonoPayloadDto> telefonos,
        List<SnapshotTelefonoPayloadDto> telefonosPersona,
        List<SnapshotTelefonoPayloadDto> telefonosEmpresa,

        List<SnapshotDireccionPayloadDto> direcciones,
        List<SnapshotDireccionPayloadDto> direccionesPersona,
        List<SnapshotDireccionPayloadDto> direccionesEmpresa
) {

    public List<SnapshotTelefonoPayloadDto> telefonosUnificados() {
        List<SnapshotTelefonoPayloadDto> result = new ArrayList<>();

        addAll(result, telefonos);
        addAll(result, telefonosPersona);
        addAll(result, telefonosEmpresa);

        return List.copyOf(result);
    }

    public List<SnapshotDireccionPayloadDto> direccionesUnificadas() {
        List<SnapshotDireccionPayloadDto> result = new ArrayList<>();

        addAll(result, direcciones);
        addAll(result, direccionesPersona);
        addAll(result, direccionesEmpresa);

        return List.copyOf(result);
    }

    private static <T> void addAll(List<T> target, List<T> source) {
        if (source == null || source.isEmpty()) {
            return;
        }

        source.stream()
                .filter(item -> item != null)
                .forEach(target::add);
    }
}