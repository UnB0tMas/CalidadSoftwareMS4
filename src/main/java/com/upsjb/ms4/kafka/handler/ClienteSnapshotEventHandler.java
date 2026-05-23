// ruta: src/main/java/com/upsjb/ms4/kafka/handler/ClienteSnapshotEventHandler.java
package com.upsjb.ms4.kafka.handler;

import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.ClienteSnapshotPayloadDto;
import com.upsjb.ms4.kafka.support.KafkaConsumedEventFactory;
import com.upsjb.ms4.kafka.support.KafkaEnvelopeReader;
import com.upsjb.ms4.mapper.snapshot.ClienteSnapshotMapper;
import com.upsjb.ms4.repository.ClienteSnapshotMs2Repository;
import com.upsjb.ms4.repository.KafkaEventoConsumidoRepository;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ClienteSnapshotEventHandler {

    private final KafkaEnvelopeReader envelopeReader;
    private final KafkaConsumedEventFactory consumedEventFactory;
    private final ClienteSnapshotMapper mapper;
    private final ClienteSnapshotMs2Repository repository;
    private final KafkaEventoConsumidoRepository consumedRepository;

    public ClienteSnapshotEventHandler(
            KafkaEnvelopeReader envelopeReader,
            KafkaConsumedEventFactory consumedEventFactory,
            ClienteSnapshotMapper mapper,
            ClienteSnapshotMs2Repository repository,
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
        DomainEventEnvelopeDto<ClienteSnapshotPayloadDto> envelope =
                envelopeReader.read(rawJson, ClienteSnapshotPayloadDto.class);

        if (alreadyConsumed(topic, partition, offset, envelope)) {
            return;
        }

        ClienteSnapshotPayloadDto payload = envelope.payload();
        validate(payload);

        ClienteSnapshotMs2 entity = repository.findByIdClienteMs2(payload.idCliente())
                .orElseGet(ClienteSnapshotMs2::new);

        mapper.updateFromPayload(entity, payload, envelope, rawJson);

        repository.save(entity);
        consumedRepository.save(consumedEventFactory.processed(topic, key, partition, offset, rawJson, envelope));
    }

    private boolean alreadyConsumed(String topic, Integer partition, Long offset, DomainEventEnvelopeDto<?> envelope) {
        return consumedRepository.existsByEventId(envelope.eventId())
                || consumedRepository.existsByTopicAndPartitionAndOffset(topic, partition, offset);
    }

    private void validate(ClienteSnapshotPayloadDto payload) {
        if (payload == null || payload.idCliente() == null || payload.idUsuarioMs1() == null) {
            throw new KafkaPublishException("El snapshot de cliente MS2 no contiene identificadores obligatorios.");
        }
    }
}