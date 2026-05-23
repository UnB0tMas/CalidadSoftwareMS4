// ruta: src/main/java/com/upsjb/ms4/repository/KafkaEventoConsumidoRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.kafka.KafkaEventoConsumido;
import com.upsjb.ms4.domain.enums.EstadoKafkaProcesamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface KafkaEventoConsumidoRepository extends
        JpaRepository<KafkaEventoConsumido, Long>,
        JpaSpecificationExecutor<KafkaEventoConsumido> {

    Optional<KafkaEventoConsumido> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);

    Optional<KafkaEventoConsumido> findByTopicAndPartitionAndOffset(String topic, Integer partition, Long offset);

    boolean existsByTopicAndPartitionAndOffset(String topic, Integer partition, Long offset);

    Page<KafkaEventoConsumido> findByEstadoProcesamientoAndEstadoTrue(
            EstadoKafkaProcesamiento estadoProcesamiento,
            Pageable pageable
    );

    Page<KafkaEventoConsumido> findByTopicAndEstadoTrue(String topic, Pageable pageable);
}