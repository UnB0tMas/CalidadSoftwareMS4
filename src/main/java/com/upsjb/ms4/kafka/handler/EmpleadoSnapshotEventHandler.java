// ruta: src/main/java/com/upsjb/ms4/kafka/handler/EmpleadoSnapshotEventHandler.java
package com.upsjb.ms4.kafka.handler;

import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.EmpleadoSnapshotPayloadDto;
import com.upsjb.ms4.kafka.support.KafkaConsumedEventFactory;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.mapper.snapshot.EmpleadoSnapshotMapper;
import com.upsjb.ms4.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmpleadoSnapshotEventHandler {

    private final KafkaEnvelopeReader envelopeReader;
    private final KafkaConsumedEventFactory consumedEventFactory;
    private final EmpleadoSnapshotMapper mapper;
    private final EmpleadoSnapshotMs2Repository repository;
    private final KafkaEventoConsumidoRepository consumedRepository;

    public EmpleadoSnapshotEventHandler(
            KafkaEnvelopeReader envelopeReader,
            KafkaConsumedEventFactory consumedEventFactory,
            EmpleadoSnapshotMapper mapper,
            EmpleadoSnapshotMs2Repository repository,
            KafkaEventoConsumidoRepository consumedRepository
    ) {
        this.envelopeReader = envelopeReader;
        this.consumedEventFactory = consumedEventFactory;
        this.mapper = mapper;
        this.repository = repository;
        this.consumedRepository = consumedRepository;
    }

    @Transactional
    public void handle(String topic, String key, Integer partition, Long offset, String rawJson) {
        DomainEventEnvelopeDto<EmpleadoSnapshotPayloadDto> envelope =
                envelopeReader.read(rawJson, EmpleadoSnapshotPayloadDto.class);

        if (alreadyConsumed(topic, partition, offset, envelope)) {
            return;
        }

        EmpleadoSnapshotPayloadDto payload = envelope.payload();
        validate(payload);

        EmpleadoSnapshotMs2 entity = repository.findByIdEmpleadoMs2(payload.idEmpleado())
                .orElseGet(EmpleadoSnapshotMs2::new);

        mapper.updateFromPayload(entity, payload, envelope, rawJson);

        repository.save(entity);
        consumedRepository.save(consumedEventFactory.processed(topic, key, partition, offset, rawJson, envelope));
    }

    private boolean alreadyConsumed(String topic, Integer partition, Long offset, DomainEventEnvelopeDto<?> envelope) {
        return consumedRepository.existsByEventId(envelope.eventId())
                || consumedRepository.existsByTopicAndPartitionAndOffset(topic, partition, offset);
    }

    private void validate(EmpleadoSnapshotPayloadDto payload) {
        if (payload == null || payload.idEmpleado() == null || payload.idUsuarioMs1() == null) {
            throw new KafkaPublishException("El snapshot de empleado MS2 no contiene identificadores obligatorios.");
        }

        if (payload.persona() == null) {
            throw new KafkaPublishException("El snapshot de empleado MS2 no contiene datos de persona.");
        }
    }
}