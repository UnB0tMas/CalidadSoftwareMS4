// ruta: src/main/java/com/upsjb/ms4/kafka/handler/MovimientoInventarioEventHandler.java
package com.upsjb.ms4.kafka.handler;

import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.MovimientoInventarioPayloadDto;
import com.upsjb.ms4.kafka.support.KafkaConsumedEventFactory;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MovimientoInventarioEventHandler {

    private final KafkaEnvelopeReader envelopeReader;
    private final KafkaConsumedEventFactory consumedEventFactory;
    private final KafkaEventoConsumidoRepository consumedRepository;

    public MovimientoInventarioEventHandler(
            KafkaEnvelopeReader envelopeReader,
            KafkaConsumedEventFactory consumedEventFactory,
            KafkaEventoConsumidoRepository consumedRepository
    ) {
        this.envelopeReader = envelopeReader;
        this.consumedEventFactory = consumedEventFactory;
        this.consumedRepository = consumedRepository;
    }

    @Transactional
    public void handle(String topic, String key, Integer partition, Long offset, String rawJson) {
        DomainEventEnvelopeDto<MovimientoInventarioPayloadDto> envelope =
                envelopeReader.read(rawJson, MovimientoInventarioPayloadDto.class);

        if (consumedRepository.existsByEventId(envelope.eventId())
                || consumedRepository.existsByTopicAndPartitionAndOffset(topic, partition, offset)) {
            return;
        }

        /*
         * Regla MS4:
         * El movimiento de inventario se registra como evento consumido para trazabilidad.
         * No altera stock_snapshot_ms3 porque el stock oficial llega por ms3.stock.snapshot.v1.
         */
        consumedRepository.save(consumedEventFactory.processed(topic, key, partition, offset, rawJson, envelope));
    }
}