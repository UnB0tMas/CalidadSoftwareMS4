// ruta: src/main/java/com/upsjb/ms4/kafka/handler/StockSnapshotEventHandler.java
package com.upsjb.ms4.kafka.handler;

import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.StockSnapshotPayloadDto;
import com.upsjb.ms4.kafka.support.KafkaConsumedEventFactory;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.repository.StockSnapshotMs3Repository;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class StockSnapshotEventHandler {

    private final KafkaEnvelopeReader envelopeReader;
    private final KafkaConsumedEventFactory consumedEventFactory;
    private final StockSnapshotMs3Repository repository;
    private final KafkaEventoConsumidoRepository consumedRepository;

    public StockSnapshotEventHandler(
            KafkaEnvelopeReader envelopeReader,
            KafkaConsumedEventFactory consumedEventFactory,
            StockSnapshotMs3Repository repository,
            KafkaEventoConsumidoRepository consumedRepository
    ) {
        this.envelopeReader = envelopeReader;
        this.consumedEventFactory = consumedEventFactory;
        this.repository = repository;
        this.consumedRepository = consumedRepository;
    }

    @Transactional
    public void handle(String topic, String key, Integer partition, Long offset, String rawJson) {
        DomainEventEnvelopeDto<StockSnapshotPayloadDto> envelope =
                envelopeReader.read(rawJson, StockSnapshotPayloadDto.class);

        if (alreadyConsumed(topic, partition, offset, envelope)) {
            return;
        }

        StockSnapshotPayloadDto payload = envelope.payload();
        validate(payload);

        StockSnapshotMs3 entity = repository.findByIdStockMs3(payload.idStock())
                .orElseGet(StockSnapshotMs3::new);

        entity.setIdStockMs3(payload.idStock());
        entity.setIdSkuMs3(payload.idSku());
        entity.setCodigoSku(payload.codigoSku());
        entity.setBarcode(payload.barcode());
        entity.setIdProductoMs3(payload.idProducto());
        entity.setCodigoProducto(payload.codigoProducto());
        entity.setNombreProducto(payload.nombreProducto());
        entity.setIdAlmacenMs3(payload.idAlmacen());
        entity.setCodigoAlmacen(payload.codigoAlmacen());
        entity.setNombreAlmacen(payload.nombreAlmacen());
        entity.setStockFisico(payload.stockFisico());
        entity.setStockReservado(payload.stockReservado());
        entity.setStockDisponible(payload.stockDisponible());
        entity.setStockMinimo(payload.stockMinimo());
        entity.setStockMaximo(payload.stockMaximo());
        entity.setCostoPromedioActual(payload.costoPromedioActual());
        entity.setUltimoCostoCompra(payload.ultimoCostoCompra());
        entity.setBajoStock(payload.bajoStock());
        entity.setSobreStock(payload.sobreStock());
        entity.setEventId(envelope.eventId());
        entity.setEventType(envelope.eventType());
        entity.setAggregateId(envelope.aggregateIdSafe());
        entity.setEventVersion(envelope.eventVersionSafe());
        entity.setOccurredAt(envelope.occurredAt());
        entity.setRequestId(envelope.requestId());
        entity.setCorrelationId(envelope.correlationId());
        entity.setPayloadJson(rawJson);
        entity.setFechaSincronizacion(LocalDateTime.now());
        entity.setEstado(Boolean.TRUE.equals(payload.estado()));

        repository.save(entity);
        consumedRepository.save(consumedEventFactory.processed(topic, key, partition, offset, rawJson, envelope));
    }

    private boolean alreadyConsumed(String topic, Integer partition, Long offset, DomainEventEnvelopeDto<?> envelope) {
        return consumedRepository.existsByEventId(envelope.eventId())
                || consumedRepository.existsByTopicAndPartitionAndOffset(topic, partition, offset);
    }

    private void validate(StockSnapshotPayloadDto payload) {
        if (payload == null || payload.idStock() == null || payload.idSku() == null || payload.idAlmacen() == null) {
            throw new KafkaPublishException("El snapshot de stock MS3 no contiene identificadores obligatorios.");
        }
    }
}