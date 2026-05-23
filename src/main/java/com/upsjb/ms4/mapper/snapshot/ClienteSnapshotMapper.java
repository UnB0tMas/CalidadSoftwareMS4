// ruta: src/main/java/com/upsjb/ms4/mapper/snapshot/ClienteSnapshotMapper.java
package com.upsjb.ms4.mapper.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.ClienteSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms2.SnapshotDireccionPayloadDto;
import com.upsjb.ms4.dto.kafka.ms2.SnapshotEmpresaPayloadDto;
import com.upsjb.ms4.dto.kafka.ms2.SnapshotPersonaPayloadDto;
import com.upsjb.ms4.dto.kafka.ms2.SnapshotTelefonoPayloadDto;
import com.upsjb.ms4.dto.kafka.ms2.SnapshotUbigeoPayloadDto;
import com.upsjb.ms4.dto.lookup.ClienteLookupResponseDto;
import com.upsjb.ms4.dto.snapshot.response.ClienteSnapshotResponseDto;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class ClienteSnapshotMapper {

    private final ObjectMapper objectMapper;

    public ClienteSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ClienteSnapshotMs2 toEntityFromPayload(
            ClienteSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (payload == null || envelope == null) {
            return null;
        }

        ClienteSnapshotMs2 entity = new ClienteSnapshotMs2();
        updateFromPayload(entity, payload, envelope, rawJson);
        return entity;
    }

    public void updateFromPayload(
            ClienteSnapshotMs2 entity,
            ClienteSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (entity == null || payload == null || envelope == null) {
            return;
        }

        SnapshotPersonaPayloadDto persona = payload.persona();
        SnapshotEmpresaPayloadDto empresa = payload.empresa();

        List<SnapshotTelefonoPayloadDto> telefonos = payload.telefonosUnificados();
        List<SnapshotDireccionPayloadDto> direcciones = payload.direccionesUnificadas();

        SnapshotTelefonoPayloadDto telefonoPrincipal = principalTelefono(telefonos);
        SnapshotDireccionPayloadDto direccionPrincipal = principalDireccion(direcciones);
        SnapshotUbigeoPayloadDto ubigeo = resolveUbigeo(direccionPrincipal, persona, empresa);

        entity.setIdClienteMs2(payload.idCliente());
        entity.setIdUsuarioMs1(payload.idUsuarioMs1());
        entity.setTipoCliente(trimToNull(payload.tipoCliente()));
        entity.setClienteActivoMs2(activeOrDefault(payload.estado()));

        mapPersona(entity, persona);
        mapEmpresa(entity, empresa);
        mapTelefonoPrincipal(entity, telefonoPrincipal);
        mapDireccionPrincipal(entity, direccionPrincipal);
        mapUbigeo(entity, ubigeo);

        entity.setPersonaJson(toJson(persona));
        entity.setEmpresaJson(toJson(empresa));
        entity.setTelefonosJson(toJson(telefonos));
        entity.setDireccionesJson(toJson(direcciones));

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

    public ClienteSnapshotResponseDto toResponse(ClienteSnapshotMs2 entity) {
        if (entity == null) {
            return null;
        }

        return new ClienteSnapshotResponseDto(
                entity.getId(),
                entity.getIdClienteMs2(),
                entity.getIdUsuarioMs1(),
                entity.getTipoCliente(),
                entity.getClienteActivoMs2(),
                entity.getIdPersonaMs2(),
                entity.getTipoDocumentoPersona(),
                entity.getNumeroDocumentoPersona(),
                entity.getNombres(),
                entity.getApePaterno(),
                entity.getApeMaterno(),
                entity.getNombreCompleto(),
                entity.getIdEmpresaMs2(),
                entity.getRuc(),
                entity.getRazonSocial(),
                entity.getNombreComercial(),
                entity.getCorreoPrincipal(),
                entity.getTelefonoPrincipal(),
                entity.getDireccionPrincipal(),
                entity.getReferenciaDireccion(),
                entity.getUbigeo(),
                entity.getDistrito(),
                entity.getProvincia(),
                entity.getDepartamento(),
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

    public ClienteLookupResponseDto toLookup(ClienteSnapshotMs2 entity) {
        if (entity == null) {
            return null;
        }

        return new ClienteLookupResponseDto(
                entity.getId(),
                entity.getIdClienteMs2(),
                entity.getIdUsuarioMs1(),
                entity.getTipoCliente(),
                entity.getTipoDocumentoPersona(),
                entity.getNumeroDocumentoPersona(),
                resolveNombreCliente(entity),
                entity.getRazonSocial(),
                entity.getCorreoPrincipal(),
                entity.getTelefonoPrincipal(),
                entity.getDireccionPrincipal(),
                entity.getClienteActivoMs2(),
                entity.getEstado()
        );
    }

    private void mapPersona(ClienteSnapshotMs2 entity, SnapshotPersonaPayloadDto persona) {
        if (persona == null) {
            entity.setIdPersonaMs2(null);
            entity.setTipoDocumentoPersona(null);
            entity.setNumeroDocumentoPersona(null);
            entity.setNombres(null);
            entity.setApePaterno(null);
            entity.setApeMaterno(null);
            entity.setNombreCompleto(null);
            return;
        }

        entity.setIdPersonaMs2(persona.idPersona());
        entity.setTipoDocumentoPersona(trimToNull(persona.tipoDoc()));
        entity.setNumeroDocumentoPersona(trimToNull(persona.numeroDoc()));
        entity.setNombres(trimToNull(persona.nombres()));
        entity.setApePaterno(trimToNull(persona.apePaterno()));
        entity.setApeMaterno(trimToNull(persona.apeMaterno()));
        entity.setNombreCompleto(nombreCompleto(persona));

        if (trimToNull(persona.correo()) != null) {
            entity.setCorreoPrincipal(trimToNull(persona.correo()));
        }
    }

    private void mapEmpresa(ClienteSnapshotMs2 entity, SnapshotEmpresaPayloadDto empresa) {
        if (empresa == null) {
            entity.setIdEmpresaMs2(null);
            entity.setRuc(null);
            entity.setRazonSocial(null);
            entity.setNombreComercial(null);
            return;
        }

        entity.setIdEmpresaMs2(empresa.idEmpresa());
        entity.setRuc(trimToNull(empresa.ruc()));
        entity.setRazonSocial(trimToNull(empresa.razonSocial()));
        entity.setNombreComercial(trimToNull(empresa.nombreComercial()));

        if (trimToNull(empresa.correo()) != null) {
            entity.setCorreoPrincipal(trimToNull(empresa.correo()));
        }
    }

    private void mapTelefonoPrincipal(ClienteSnapshotMs2 entity, SnapshotTelefonoPayloadDto telefono) {
        entity.setTelefonoPrincipal(telefono == null ? null : trimToNull(telefono.numero()));
    }

    private void mapDireccionPrincipal(ClienteSnapshotMs2 entity, SnapshotDireccionPayloadDto direccion) {
        if (direccion == null) {
            entity.setDireccionPrincipal(null);
            entity.setReferenciaDireccion(null);
            return;
        }

        entity.setDireccionPrincipal(trimToNull(direccion.direccion()));
        entity.setReferenciaDireccion(trimToNull(direccion.referencia()));
    }

    private void mapUbigeo(ClienteSnapshotMs2 entity, SnapshotUbigeoPayloadDto ubigeo) {
        if (ubigeo == null) {
            entity.setUbigeo(null);
            entity.setDistrito(null);
            entity.setProvincia(null);
            entity.setDepartamento(null);
            return;
        }

        entity.setUbigeo(ubigeo.idDistrito() == null ? null : String.valueOf(ubigeo.idDistrito()));
        entity.setDistrito(trimToNull(ubigeo.distrito()));
        entity.setProvincia(trimToNull(ubigeo.provincia()));
        entity.setDepartamento(trimToNull(ubigeo.departamento()));
    }

    private SnapshotTelefonoPayloadDto principalTelefono(List<SnapshotTelefonoPayloadDto> telefonos) {
        if (telefonos == null || telefonos.isEmpty()) {
            return null;
        }

        return telefonos.stream()
                .filter(item -> item != null && activeOrDefault(item.estado()))
                .sorted(Comparator.comparing(item -> !Boolean.TRUE.equals(item.esPrincipal())))
                .findFirst()
                .orElse(null);
    }

    private SnapshotDireccionPayloadDto principalDireccion(List<SnapshotDireccionPayloadDto> direcciones) {
        if (direcciones == null || direcciones.isEmpty()) {
            return null;
        }

        return direcciones.stream()
                .filter(item -> item != null && activeOrDefault(item.estado()))
                .sorted(Comparator.comparing(item -> !Boolean.TRUE.equals(item.esPrincipal())))
                .findFirst()
                .orElse(null);
    }

    private SnapshotUbigeoPayloadDto resolveUbigeo(
            SnapshotDireccionPayloadDto direccion,
            SnapshotPersonaPayloadDto persona,
            SnapshotEmpresaPayloadDto empresa
    ) {
        if (direccion != null && direccion.ubigeo() != null) {
            return direccion.ubigeo();
        }

        if (persona != null && persona.distritoDocumento() != null) {
            return persona.distritoDocumento();
        }

        if (empresa != null && empresa.distritoRegistro() != null) {
            return empresa.distritoRegistro();
        }

        return null;
    }

    private String resolveNombreCliente(ClienteSnapshotMs2 entity) {
        String nombreCompleto = trimToNull(entity.getNombreCompleto());
        if (nombreCompleto != null) {
            return nombreCompleto;
        }

        String razonSocial = trimToNull(entity.getRazonSocial());
        if (razonSocial != null) {
            return razonSocial;
        }

        return trimToNull(entity.getNombreComercial());
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
            throw new KafkaPublishException("No se pudo serializar parte del snapshot de cliente MS2.", ex);
        }
    }
}