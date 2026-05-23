// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/response/ClienteSnapshotResponseDto.java
package com.upsjb.ms4.dto.snapshot.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteSnapshotResponseDto(
        Long id,
        Long idClienteMs2,
        Long idUsuarioMs1,
        String tipoCliente,
        Boolean clienteActivoMs2,
        Long idPersonaMs2,
        String tipoDocumentoPersona,
        String numeroDocumentoPersona,
        String nombres,
        String apePaterno,
        String apeMaterno,
        String nombreCompleto,
        Long idEmpresaMs2,
        String ruc,
        String razonSocial,
        String nombreComercial,
        String correoPrincipal,
        String telefonoPrincipal,
        String direccionPrincipal,
        String referenciaDireccion,
        String ubigeo,
        String distrito,
        String provincia,
        String departamento,
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