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
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ProductoSnapshotMapper {

    private final ObjectMapper objectMapper;

    public ProductoSnapshotMapper(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public ProductoSnapshotMs3 toEntityFromPayload(
            ProductoSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (
                payload == null
                        || envelope == null
        ) {
            return null;
        }

        ProductoSnapshotMs3 entity =
                new ProductoSnapshotMs3();

        updateFromPayload(
                entity,
                payload,
                envelope,
                rawJson
        );

        return entity;
    }

    public void updateFromPayload(
            ProductoSnapshotMs3 entity,
            ProductoSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String rawJson
    ) {
        if (
                entity == null
                        || payload == null
                        || envelope == null
        ) {
            return;
        }

        entity.setSnapshotCompleto(
                Boolean.TRUE.equals(
                        payload.snapshotCompleto()
                )
        );

        entity.setSnapshotGeneradoAt(
                payload.snapshotGeneradoAt()
        );

        entity.setIdProductoMs3(
                payload.idProducto()
        );

        entity.setCodigoProducto(
                trimToNull(
                        payload.codigoProducto()
                )
        );

        entity.setNombre(
                trimToNull(
                        payload.nombre()
                )
        );

        entity.setSlug(
                trimToNull(
                        payload.slug()
                )
        );

        entity.setIdCategoriaMs3(
                payload.idCategoria()
        );

        entity.setCodigoCategoria(
                trimToNull(
                        payload.codigoCategoria()
                )
        );

        entity.setNombreCategoria(
                trimToNull(
                        payload.nombreCategoria()
                )
        );

        entity.setSlugCategoria(
                trimToNull(
                        payload.slugCategoria()
                )
        );

        entity.setNivelCategoria(
                payload.nivelCategoria()
        );

        entity.setOrdenCategoria(
                payload.ordenCategoria()
        );

        entity.setCategoriaPermiteProductos(
                Boolean.TRUE.equals(
                        payload.categoriaPermiteProductos()
                )
        );

        entity.setCategoriaEstado(
                Boolean.TRUE.equals(
                        payload.categoriaEstado()
                )
        );

        entity.setIdCategoriaPadreMs3(
                payload.idCategoriaPadre()
        );

        entity.setCodigoCategoriaPadre(
                trimToNull(
                        payload.codigoCategoriaPadre()
                )
        );

        entity.setNombreCategoriaPadre(
                trimToNull(
                        payload.nombreCategoriaPadre()
                )
        );

        entity.setSlugCategoriaPadre(
                trimToNull(
                        payload.slugCategoriaPadre()
                )
        );

        entity.setCategoriaRutaCodigo(
                trimToNull(
                        payload.categoriaRutaCodigo()
                )
        );

        entity.setCategoriaRutaNombre(
                trimToNull(
                        payload.categoriaRutaNombre()
                )
        );

        entity.setCategoriaRutaJson(
                toJson(
                        payload.categoriaRuta()
                )
        );

        entity.setIdMarcaMs3(
                payload.idMarca()
        );

        entity.setCodigoMarca(
                trimToNull(
                        payload.codigoMarca()
                )
        );

        entity.setNombreMarca(
                trimToNull(
                        payload.nombreMarca()
                )
        );

        entity.setSlugMarca(
                trimToNull(
                        payload.slugMarca()
                )
        );

        entity.setMarcaEstado(
                payload.marcaEstado() == null
                        ? null
                        : Boolean.TRUE.equals(
                        payload.marcaEstado()
                )
        );

        entity.setDescripcionCorta(
                trimToNull(
                        payload.descripcionCorta()
                )
        );

        entity.setDescripcionLarga(
                trimToNull(
                        payload.descripcionLarga()
                )
        );

        entity.setGeneroObjetivo(
                trimToNull(
                        payload.generoObjetivo()
                )
        );

        entity.setTemporada(
                trimToNull(
                        payload.temporada()
                )
        );

        entity.setDeporte(
                trimToNull(
                        payload.deporte()
                )
        );

        entity.setEstadoRegistro(
                trimToNull(
                        payload.estadoRegistro()
                )
        );

        entity.setEstadoPublicacion(
                trimToNull(
                        payload.estadoPublicacion()
                )
        );

        entity.setEstadoVenta(
                trimToNull(
                        payload.estadoVenta()
                )
        );

        entity.setVisiblePublico(
                Boolean.TRUE.equals(
                        payload.visiblePublico()
                )
        );

        entity.setVendible(
                Boolean.TRUE.equals(
                        payload.vendible()
                )
        );

        entity.setFechaPublicacionInicio(
                payload.fechaPublicacionInicio()
        );

        entity.setFechaPublicacionFin(
                payload.fechaPublicacionFin()
        );

        entity.setMotivoEstado(
                trimToNull(
                        payload.motivoEstado()
                )
        );

        entity.setImagenPrincipalUrl(
                trimToNull(
                        payload.imagenPrincipalUrl()
                )
        );

        entity.setPlantillaAtributosJson(
                toJson(
                        payload.plantillaAtributos()
                )
        );

        entity.setAtributosJson(
                toJson(
                        payload.atributos()
                )
        );

        entity.setSkusJson(
                toJson(
                        payload.skus()
                )
        );

        entity.setImagenesJson(
                toJson(
                        payload.imagenes()
                )
        );

        entity.setEventId(
                envelope.eventId()
        );

        entity.setEventType(
                envelope.eventType()
        );

        entity.setAggregateId(
                envelope.aggregateIdSafe()
        );

        entity.setEventVersion(
                envelope.eventVersionSafe()
        );

        entity.setOccurredAt(
                envelope.occurredAt()
        );

        entity.setRequestId(
                envelope.requestId()
        );

        entity.setCorrelationId(
                envelope.correlationId()
        );

        entity.setPayloadJson(rawJson);

        entity.setFechaSincronizacion(
                LocalDateTime.now()
        );

        entity.setEstado(
                activeOrDefault(
                        payload.estado()
                )
        );
    }

    public ProductoVentaResponseDto toResponse(
            ProductoSnapshotMs3 entity
    ) {
        if (entity == null) {
            return null;
        }

        return new ProductoVentaResponseDto(
                entity.getId(),
                entity.getSnapshotCompleto(),
                entity.getSnapshotGeneradoAt(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getNombre(),
                entity.getSlug(),
                entity.getIdCategoriaMs3(),
                entity.getCodigoCategoria(),
                entity.getNombreCategoria(),
                entity.getSlugCategoria(),
                entity.getNivelCategoria(),
                entity.getOrdenCategoria(),
                entity.getCategoriaPermiteProductos(),
                entity.getCategoriaEstado(),
                entity.getIdCategoriaPadreMs3(),
                entity.getCodigoCategoriaPadre(),
                entity.getNombreCategoriaPadre(),
                entity.getSlugCategoriaPadre(),
                entity.getCategoriaRutaCodigo(),
                entity.getCategoriaRutaNombre(),
                entity.getCategoriaRutaJson(),
                entity.getIdMarcaMs3(),
                entity.getCodigoMarca(),
                entity.getNombreMarca(),
                entity.getSlugMarca(),
                entity.getMarcaEstado(),
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
                entity.getImagenPrincipalUrl(),
                entity.getPlantillaAtributosJson(),
                entity.getAtributosJson(),
                entity.getSkusJson(),
                entity.getImagenesJson(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getAggregateId(),
                entity.getEventVersion(),
                entity.getOccurredAt(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getFechaSincronizacion(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ProductoLookupResponseDto toLookup(
            ProductoSnapshotMs3 entity
    ) {
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

    private Boolean activeOrDefault(
            Boolean value
    ) {
        return value == null
                || Boolean.TRUE.equals(value);
    }

    private String trimToNull(
            String value
    ) {
        return value == null
                || value.isBlank()
                ? null
                : value.trim();
    }

    private String toJson(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper
                    .writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new KafkaPublishException(
                    "No se pudo serializar una sección del snapshot de producto MS3.",
                    ex
            );
        }
    }
}