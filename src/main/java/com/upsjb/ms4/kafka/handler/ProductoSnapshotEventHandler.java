package com.upsjb.ms4.kafka.handler;

import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.ProductoSnapshotPayloadDto;
import com.upsjb.ms4.kafka.support.KafkaConsumedEventFactory;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.service.contract.snapshot.CatalogoVentaSnapshotService;
import com.upsjb.ms4.shared.exception.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductoSnapshotEventHandler {

    private static final int PRODUCTO_SNAPSHOT_SCHEMA_VERSION =
            2;

    private static final String PRODUCTO_AGGREGATE_TYPE =
            "PRODUCTO";

    private final KafkaEnvelopeReader envelopeReader;
    private final KafkaConsumedEventFactory consumedEventFactory;
    private final KafkaEventoConsumidoRepository consumedRepository;
    private final CatalogoVentaSnapshotService
            catalogoVentaSnapshotService;

    public ProductoSnapshotEventHandler(
            KafkaEnvelopeReader envelopeReader,
            KafkaConsumedEventFactory consumedEventFactory,
            KafkaEventoConsumidoRepository consumedRepository,
            CatalogoVentaSnapshotService catalogoVentaSnapshotService
    ) {
        this.envelopeReader = envelopeReader;
        this.consumedEventFactory = consumedEventFactory;
        this.consumedRepository = consumedRepository;
        this.catalogoVentaSnapshotService =
                catalogoVentaSnapshotService;
    }

    @Transactional
    public void handle(
            String topic,
            String key,
            Integer partition,
            Long offset,
            String rawJson
    ) {
        DomainEventEnvelopeDto<ProductoSnapshotPayloadDto> envelope =
                envelopeReader.read(
                        rawJson,
                        ProductoSnapshotPayloadDto.class
                );

        if (
                consumedRepository.existsByEventId(
                        envelope.eventId()
                )
                        || consumedRepository
                        .existsByTopicAndPartitionAndOffset(
                                topic,
                                partition,
                                offset
                        )
        ) {
            return;
        }

        validateEnvelope(envelope);
        validatePayload(envelope.payload());

        catalogoVentaSnapshotService
                .procesarProductoSnapshot(
                        envelope,
                        rawJson
                );

        consumedRepository.save(
                consumedEventFactory.processed(
                        topic,
                        key,
                        partition,
                        offset,
                        rawJson,
                        envelope
                )
        );
    }

    private void validateEnvelope(
            DomainEventEnvelopeDto<ProductoSnapshotPayloadDto> envelope
    ) {
        if (envelope == null) {
            throw new ValidationException(
                    "El envelope del snapshot de producto MS3 es obligatorio."
            );
        }

        if (
                envelope.eventVersionSafe()
                        != PRODUCTO_SNAPSHOT_SCHEMA_VERSION
        ) {
            throw new ValidationException(
                    "MS4 solo admite ms3.producto.snapshot.v2 con schemaVersion=2."
            );
        }

        if (
                envelope.aggregateType() == null
                        || !PRODUCTO_AGGREGATE_TYPE
                        .equalsIgnoreCase(
                                envelope.aggregateType().trim()
                        )
        ) {
            throw new ValidationException(
                    "El aggregateType del snapshot debe ser PRODUCTO."
            );
        }
    }

    private void validatePayload(
            ProductoSnapshotPayloadDto payload
    ) {
        if (
                payload == null
                        || payload.idProducto() == null
                        || payload.idProducto() <= 0
        ) {
            throw new ValidationException(
                    "El snapshot no contiene un idProducto válido."
            );
        }

        if (!Boolean.TRUE.equals(
                payload.snapshotCompleto()
        )) {
            throw new ValidationException(
                    "MS4 requiere un snapshot completo de producto."
            );
        }

        if (payload.idCategoria() == null) {
            throw new ValidationException(
                    "El producto no contiene una categoría."
            );
        }

        if (!Boolean.TRUE.equals(
                payload.categoriaPermiteProductos()
        )) {
            throw new ValidationException(
                    "El producto debe pertenecer a una categoría final."
            );
        }
    }
}