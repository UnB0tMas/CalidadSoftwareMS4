// ruta: src/main/java/com/upsjb/ms4/mapper/snapshot/ProductoSnapshotMapper.java
package com.upsjb.ms4.mapper.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.ProductoSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.ProductoLookupResponseDto;
import com.upsjb.ms4.dto.snapshot.response.ProductoVentaResponseDto;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductoSnapshotMapper {

    private final ObjectMapper objectMapper;

    public ProductoSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProductoSnapshotMs3 toEntityFromPayload(
            ProductoSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (payload == null || envelope == null) {
            return null;
        }

        ProductoSnapshotMs3 entity = new ProductoSnapshotMs3();
        updateFromPayload(entity, payload, envelope, rawJson);
        return entity;
    }

    public void updateFromPayload(
            ProductoSnapshotMs3 entity,
            ProductoSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (entity == null || payload == null || envelope == null) {
            return;
        }

        entity.setIdProductoMs3(payload.idProducto());
        entity.setCodigoProducto(trimToNull(payload.codigoProducto()));
        entity.setNombre(trimToNull(payload.nombre()));
        entity.setSlug(trimToNull(payload.slug()));

        entity.setIdTipoProductoMs3(payload.idTipoProducto());
        entity.setCodigoTipoProducto(trimToNull(payload.codigoTipoProducto()));
        entity.setNombreTipoProducto(trimToNull(payload.nombreTipoProducto()));

        entity.setIdCategoriaMs3(payload.idCategoria());
        entity.setCodigoCategoria(trimToNull(payload.codigoCategoria()));
        entity.setNombreCategoria(trimToNull(payload.nombreCategoria()));
        entity.setSlugCategoria(trimToNull(payload.slugCategoria()));

        entity.setIdMarcaMs3(payload.idMarca());
        entity.setCodigoMarca(trimToNull(payload.codigoMarca()));
        entity.setNombreMarca(trimToNull(payload.nombreMarca()));
        entity.setSlugMarca(trimToNull(payload.slugMarca()));

        entity.setDescripcionCorta(trimToNull(payload.descripcionCorta()));
        entity.setDescripcionLarga(trimToNull(payload.descripcionLarga()));
        entity.setGeneroObjetivo(trimToNull(payload.generoObjetivo()));
        entity.setTemporada(trimToNull(payload.temporada()));
        entity.setDeporte(trimToNull(payload.deporte()));

        entity.setEstadoRegistro(trimToNull(payload.estadoRegistro()));
        entity.setEstadoPublicacion(trimToNull(payload.estadoPublicacion()));
        entity.setEstadoVenta(trimToNull(payload.estadoVenta()));
        entity.setVisiblePublico(Boolean.TRUE.equals(payload.visiblePublico()));
        entity.setVendible(Boolean.TRUE.equals(payload.vendible()));
        entity.setFechaPublicacionInicio(payload.fechaPublicacionInicio());
        entity.setFechaPublicacionFin(payload.fechaPublicacionFin());
        entity.setMotivoEstado(trimToNull(payload.motivoEstado()));

        entity.setAtributosJson(toJson(payload.atributos()));
        entity.setSkusJson(toJson(payload.skus()));
        entity.setImagenesJson(toJson(payload.imagenes()));

        entity.setEventId(envelope.eventId());
        entity.setEventType(envelope.eventType());
        entity.setAggregateId(envelope.aggregateIdSafe());
        entity.setEventVersion(envelope.eventVersionSafe());
        entity.setOccurredAt(envelope.occurredAt());
        entity.setRequestId(envelope.requestId());
        entity.setCorrelationId(envelope.correlationId());
        entity.setPayloadJson(rawJson);
        entity.setFechaSincronizacion(LocalDateTime.now());

        /*
         * Estado lógico del snapshot recibido desde MS3.
         * No se mezcla con visiblePublico ni vendible: esos campos son reglas comerciales,
         * no estado lógico de persistencia.
         */
        entity.setEstado(activeOrDefault(payload.estado()));
    }

    public ProductoVentaResponseDto toResponse(ProductoSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new ProductoVentaResponseDto(
                entity.getId(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getNombre(),
                entity.getSlug(),
                entity.getIdTipoProductoMs3(),
                entity.getCodigoTipoProducto(),
                entity.getNombreTipoProducto(),
                entity.getIdCategoriaMs3(),
                entity.getCodigoCategoria(),
                entity.getNombreCategoria(),
                entity.getSlugCategoria(),
                entity.getIdMarcaMs3(),
                entity.getCodigoMarca(),
                entity.getNombreMarca(),
                entity.getSlugMarca(),
                entity.getDescripcionCorta(),
                entity.getDescripcionLarga(),
                entity.getGeneroObjetivo(),
                entity.getTemporada(),
                entity.getDeporte(),
                entity.getEstadoRegistro(),
                entity.getEstadoPublicacion(),
                entity.getEstadoVenta(),
                entity.getVisiblePublico(),
                entity.getVendible(),
                entity.getFechaPublicacionInicio(),
                entity.getFechaPublicacionFin(),
                entity.getMotivoEstado(),
                entity.getAtributosJson(),
                entity.getSkusJson(),
                entity.getImagenesJson(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getAggregateId(),
                entity.getEventVersion(),
                entity.getOccurredAt(),
                entity.getFechaSincronizacion(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ProductoLookupResponseDto toLookup(ProductoSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new ProductoLookupResponseDto(
                entity.getId(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getNombre(),
                entity.getSlug(),
                entity.getNombreCategoria(),
                entity.getNombreMarca(),
                entity.getEstadoPublicacion(),
                entity.getEstadoVenta(),
                entity.getVisiblePublico(),
                entity.getVendible(),
                entity.getEstado()
        );
    }

    private Boolean activeOrDefault(Boolean value) {
        return value == null || Boolean.TRUE.equals(value);
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
            throw new KafkaPublishException("No se pudo serializar una sección del snapshot de producto MS3.", ex);
        }
    }
}