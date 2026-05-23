// ruta: src/main/java/com/upsjb/ms4/kafka/handler/ProductoSnapshotEventHandler.java
package com.upsjb.ms4.kafka.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.ProductoSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.SkuSnapshotPayloadDto;
import com.upsjb.ms4.kafka.support.KafkaConsumedEventFactory;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.repository.ProductoSnapshotMs3Repository;
import com.upsjb.ms4.repository.SkuSnapshotMs3Repository;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ProductoSnapshotEventHandler {

    private final KafkaEnvelopeReader envelopeReader;
    private final KafkaConsumedEventFactory consumedEventFactory;
    private final ObjectMapper objectMapper;
    private final ProductoSnapshotMs3Repository productoRepository;
    private final SkuSnapshotMs3Repository skuRepository;
    private final KafkaEventoConsumidoRepository consumedRepository;

    public ProductoSnapshotEventHandler(
            KafkaEnvelopeReader envelopeReader,
            KafkaConsumedEventFactory consumedEventFactory,
            ObjectMapper objectMapper,
            ProductoSnapshotMs3Repository productoRepository,
            SkuSnapshotMs3Repository skuRepository,
            KafkaEventoConsumidoRepository consumedRepository
    ) {
        this.envelopeReader = envelopeReader;
        this.consumedEventFactory = consumedEventFactory;
        this.objectMapper = objectMapper;
        this.productoRepository = productoRepository;
        this.skuRepository = skuRepository;
        this.consumedRepository = consumedRepository;
    }

    @Transactional
    public void handle(String topic, String key, Integer partition, Long offset, String rawJson) {
        DomainEventEnvelopeDto<ProductoSnapshotPayloadDto> envelope =
                envelopeReader.read(rawJson, ProductoSnapshotPayloadDto.class);

        if (alreadyConsumed(topic, partition, offset, envelope)) {
            return;
        }

        ProductoSnapshotPayloadDto payload = envelope.payload();
        validate(payload);

        ProductoSnapshotMs3 producto = productoRepository.findByIdProductoMs3(payload.idProducto())
                .orElseGet(ProductoSnapshotMs3::new);

        producto.setIdProductoMs3(payload.idProducto());
        producto.setCodigoProducto(payload.codigoProducto());
        producto.setNombre(payload.nombre());
        producto.setSlug(payload.slug());
        producto.setIdTipoProductoMs3(payload.idTipoProducto());
        producto.setCodigoTipoProducto(payload.codigoTipoProducto());
        producto.setNombreTipoProducto(payload.nombreTipoProducto());
        producto.setIdCategoriaMs3(payload.idCategoria());
        producto.setCodigoCategoria(payload.codigoCategoria());
        producto.setNombreCategoria(payload.nombreCategoria());
        producto.setSlugCategoria(payload.slugCategoria());
        producto.setIdMarcaMs3(payload.idMarca());
        producto.setCodigoMarca(payload.codigoMarca());
        producto.setNombreMarca(payload.nombreMarca());
        producto.setSlugMarca(payload.slugMarca());
        producto.setDescripcionCorta(payload.descripcionCorta());
        producto.setDescripcionLarga(payload.descripcionLarga());
        producto.setGeneroObjetivo(payload.generoObjetivo());
        producto.setTemporada(payload.temporada());
        producto.setDeporte(payload.deporte());
        producto.setEstadoRegistro(payload.estadoRegistro());
        producto.setEstadoPublicacion(payload.estadoPublicacion());
        producto.setEstadoVenta(payload.estadoVenta());
        producto.setVisiblePublico(Boolean.TRUE.equals(payload.visiblePublico()));
        producto.setVendible(Boolean.TRUE.equals(payload.vendible()));
        producto.setFechaPublicacionInicio(payload.fechaPublicacionInicio());
        producto.setFechaPublicacionFin(payload.fechaPublicacionFin());
        producto.setMotivoEstado(payload.motivoEstado());
        producto.setAtributosJson(toJson(payload.atributos()));
        producto.setSkusJson(toJson(payload.skus()));
        producto.setImagenesJson(toJson(payload.imagenes()));
        producto.setEventId(envelope.eventId());
        producto.setEventType(envelope.eventType());
        producto.setAggregateId(envelope.aggregateIdSafe());
        producto.setEventVersion(envelope.eventVersionSafe());
        producto.setOccurredAt(envelope.occurredAt());
        producto.setRequestId(envelope.requestId());
        producto.setCorrelationId(envelope.correlationId());
        producto.setPayloadJson(rawJson);
        producto.setFechaSincronizacion(LocalDateTime.now());
        producto.setEstado(activeOrDefault(payload.estado()));

        productoRepository.save(producto);
        upsertSkus(payload.skus(), envelope, rawJson);
        consumedRepository.save(consumedEventFactory.processed(topic, key, partition, offset, rawJson, envelope));
    }

    private void upsertSkus(
            List<SkuSnapshotPayloadDto> skus,
            DomainEventEnvelopeDto<ProductoSnapshotPayloadDto> envelope,
            String rawJson
    ) {
        if (skus == null || skus.isEmpty()) {
            return;
        }

        for (SkuSnapshotPayloadDto payload : skus) {
            if (payload == null || payload.idSku() == null) {
                continue;
            }

            SkuSnapshotMs3 sku = skuRepository.findByIdSkuMs3(payload.idSku())
                    .orElseGet(SkuSnapshotMs3::new);

            sku.setIdSkuMs3(payload.idSku());
            sku.setIdProductoMs3(payload.idProducto());
            sku.setCodigoProducto(payload.codigoProducto());
            sku.setCodigoSku(payload.codigoSku());
            sku.setBarcode(payload.barcode());
            sku.setColor(payload.color());
            sku.setTalla(payload.talla());
            sku.setMaterial(payload.material());
            sku.setModelo(payload.modelo());
            sku.setStockMinimo(payload.stockMinimo());
            sku.setStockMaximo(payload.stockMaximo());
            sku.setPesoGramos(payload.pesoGramos());
            sku.setAltoCm(payload.altoCm());
            sku.setAnchoCm(payload.anchoCm());
            sku.setLargoCm(payload.largoCm());
            sku.setEstadoSku(payload.estadoSku());
            sku.setAtributosJson(toJson(payload.atributos()));
            sku.setEventId(envelope.eventId());
            sku.setEventType(envelope.eventType());
            sku.setAggregateId(String.valueOf(payload.idSku()));
            sku.setEventVersion(envelope.eventVersionSafe());
            sku.setOccurredAt(envelope.occurredAt());
            sku.setRequestId(envelope.requestId());
            sku.setCorrelationId(envelope.correlationId());
            sku.setPayloadJson(rawJson);
            sku.setFechaSincronizacion(LocalDateTime.now());
            sku.setEstado(activeOrDefault(payload.estado()));

            skuRepository.save(sku);
        }
    }

    private boolean alreadyConsumed(String topic, Integer partition, Long offset, DomainEventEnvelopeDto<?> envelope) {
        return consumedRepository.existsByEventId(envelope.eventId())
                || consumedRepository.existsByTopicAndPartitionAndOffset(topic, partition, offset);
    }

    private void validate(ProductoSnapshotPayloadDto payload) {
        if (payload == null || payload.idProducto() == null) {
            throw new KafkaPublishException("El snapshot de producto MS3 no contiene idProducto.");
        }
    }

    private Boolean activeOrDefault(Boolean estado) {
        return estado == null || Boolean.TRUE.equals(estado);
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