// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/response/EmpleadoSnapshotResponseDto.java
package com.upsjb.ms4.dto.snapshot.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmpleadoSnapshotResponseDto(
        Long id,
        Long idEmpleadoMs2,
        Long idUsuarioMs1,
        String codigoEmpleado,
        Boolean empleadoActivoMs2,
        Long idAreaMs2,
        String areaCodigo,
        String areaNombre,
        Long idPersonaMs2,
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String apePaterno,
        String apeMaterno,
        String nombreCompleto,
        String correo,
        String telefonoPrincipal,
        LocalDate fechaIngreso,
        LocalDate fechaCese,
        Boolean puedeCrear,
        Boolean puedeActualizar,
        UUID eventId,
        String eventType,
        String aggregateId,
        Integer eventVersion,
        String producer,
        LocalDateTime occurredAt,
        String requestId,
        String correlationId,
        LocalDateTime fechaSincronizacion,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}