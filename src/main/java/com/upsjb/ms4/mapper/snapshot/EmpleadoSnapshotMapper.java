// ruta: src/main/java/com/upsjb/ms4/mapper/snapshot/EmpleadoSnapshotMapper.java
package com.upsjb.ms4.mapper.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.EmpleadoSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms2.SnapshotPersonaPayloadDto;
import com.upsjb.ms4.dto.lookup.EmpleadoLookupResponseDto;
import com.upsjb.ms4.dto.snapshot.response.EmpleadoSnapshotResponseDto;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EmpleadoSnapshotMapper {

    private final ObjectMapper objectMapper;

    public EmpleadoSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EmpleadoSnapshotMs2 toEntityFromPayload(
            EmpleadoSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (payload == null || envelope == null) {
            return null;
        }

        EmpleadoSnapshotMs2 entity = new EmpleadoSnapshotMs2();
        updateFromPayload(entity, payload, envelope, rawJson);
        return entity;
    }

    public void updateFromPayload(
            EmpleadoSnapshotMs2 entity,
            EmpleadoSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (entity == null || payload == null || envelope == null) {
            return;
        }

        SnapshotPersonaPayloadDto persona = payload.persona();

        entity.setIdEmpleadoMs2(payload.idEmpleado());
        entity.setIdUsuarioMs1(payload.idUsuarioMs1());
        entity.setCodigoEmpleado(trimToNull(payload.codigoEmpleado()));
        entity.setEmpleadoActivoMs2(activeOrDefault(payload.estado()));
        entity.setIdAreaMs2(payload.idAreaResolved());
        entity.setAreaCodigo(trimToNull(payload.areaCodigoResolved()));
        entity.setAreaNombre(trimToNull(payload.areaNombreResolved()));

        mapPersona(entity, persona);

        entity.setFechaIngreso(payload.fechaIngreso());
        entity.setFechaCese(payload.fechaCese());
        entity.setPuedeCrear(Boolean.TRUE.equals(payload.puedeCrear()));
        entity.setPuedeActualizar(Boolean.TRUE.equals(payload.puedeActualizar()));
        entity.setPersonaJson(toJson(persona));

        entity.setEventId(envelope.eventId());
        entity.setEventType(envelope.eventType());
        entity.setAggregateId(envelope.aggregateIdSafe());
        entity.setEventVersion(envelope.eventVersionSafe());
        entity.setProducer(envelope.producerSafe());
        entity.setOccurredAt(envelope.occurredAt());
        entity.setRequestId(envelope.requestId());
        entity.setCorrelationId(envelope.correlationId());
        entity.setPayloadJson(rawJson);
        entity.setFechaSincronizacion(LocalDateTime.now());
        entity.setEstado(activeOrDefault(payload.estado()));
    }

    public EmpleadoSnapshotResponseDto toResponse(EmpleadoSnapshotMs2 entity) {
        if (entity == null) {
            return null;
        }

        return new EmpleadoSnapshotResponseDto(
                entity.getId(),
                entity.getIdEmpleadoMs2(),
                entity.getIdUsuarioMs1(),
                entity.getCodigoEmpleado(),
                entity.getEmpleadoActivoMs2(),
                entity.getIdAreaMs2(),
                entity.getAreaCodigo(),
                entity.getAreaNombre(),
                entity.getIdPersonaMs2(),
                entity.getTipoDocumento(),
                entity.getNumeroDocumento(),
                entity.getNombres(),
                entity.getApePaterno(),
                entity.getApeMaterno(),
                entity.getNombreCompleto(),
                entity.getCorreo(),
                entity.getTelefonoPrincipal(),
                entity.getFechaIngreso(),
                entity.getFechaCese(),
                entity.getPuedeCrear(),
                entity.getPuedeActualizar(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getAggregateId(),
                entity.getEventVersion(),
                entity.getProducer(),
                entity.getOccurredAt(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getFechaSincronizacion(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public EmpleadoLookupResponseDto toLookup(EmpleadoSnapshotMs2 entity) {
        if (entity == null) {
            return null;
        }

        return new EmpleadoLookupResponseDto(
                entity.getId(),
                entity.getIdEmpleadoMs2(),
                entity.getIdUsuarioMs1(),
                entity.getCodigoEmpleado(),
                entity.getNombreCompleto(),
                entity.getTipoDocumento(),
                entity.getNumeroDocumento(),
                entity.getCorreo(),
                entity.getAreaCodigo(),
                entity.getAreaNombre(),
                entity.getEmpleadoActivoMs2(),
                entity.getEstado()
        );
    }

    private void mapPersona(EmpleadoSnapshotMs2 entity, SnapshotPersonaPayloadDto persona) {
        if (persona == null) {
            entity.setIdPersonaMs2(null);
            entity.setTipoDocumento(null);
            entity.setNumeroDocumento(null);
            entity.setNombres(null);
            entity.setApePaterno(null);
            entity.setApeMaterno(null);
            entity.setNombreCompleto(null);
            entity.setCorreo(null);
            return;
        }

        entity.setIdPersonaMs2(persona.idPersona());
        entity.setTipoDocumento(trimToNull(persona.tipoDoc()));
        entity.setNumeroDocumento(trimToNull(persona.numeroDoc()));
        entity.setNombres(trimToNull(persona.nombres()));
        entity.setApePaterno(trimToNull(persona.apePaterno()));
        entity.setApeMaterno(trimToNull(persona.apeMaterno()));
        entity.setNombreCompleto(nombreCompleto(persona));
        entity.setCorreo(trimToNull(persona.correo()));
    }

    private String nombreCompleto(SnapshotPersonaPayloadDto persona) {
        String value = String.join(
                " ",
                nullToEmpty(persona.nombres()),
                nullToEmpty(persona.apePaterno()),
                nullToEmpty(persona.apeMaterno())
        ).trim();

        return value.isBlank() ? null : value;
    }

    private Boolean activeOrDefault(Boolean value) {
        return value == null || Boolean.TRUE.equals(value);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new KafkaPublishException("No se pudo serializar persona del snapshot de empleado MS2.", ex);
        }
    }
}