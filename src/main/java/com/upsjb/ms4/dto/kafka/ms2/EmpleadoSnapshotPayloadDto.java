// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms2/EmpleadoSnapshotPayloadDto.java
package com.upsjb.ms4.dto.kafka.ms2;

import java.time.LocalDate;

public record EmpleadoSnapshotPayloadDto(
        Long idEmpleado,
        Long idUsuarioMs1,
        String codigoEmpleado,
        Boolean estado,

        Long idArea,
        String areaCodigo,
        String areaNombre,
        SnapshotAreaPayloadDto area,

        SnapshotPersonaPayloadDto persona,
        LocalDate fechaIngreso,
        LocalDate fechaCese,
        Boolean puedeCrear,
        Boolean puedeActualizar
) {

    public Long idAreaResolved() {
        return idArea != null ? idArea : area == null ? null : area.id();
    }

    public String areaCodigoResolved() {
        return notBlank(areaCodigo) ? areaCodigo : area == null ? null : area.codigo();
    }

    public String areaNombreResolved() {
        return notBlank(areaNombre) ? areaNombre : area == null ? null : area.nombre();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}