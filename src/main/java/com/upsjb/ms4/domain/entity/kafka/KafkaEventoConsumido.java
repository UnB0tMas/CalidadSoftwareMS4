// ruta: src/main/java/com/upsjb/ms4/domain/entity/kafka/KafkaEventoConsumido.java
package com.upsjb.ms4.domain.entity.kafka;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.enums.EstadoKafkaProcesamiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "kafka_evento_consumido",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_kafka_evento_consumido_event_id",
                        columnNames = "event_id"
                ),
                @UniqueConstraint(
                        name = "uk_kafka_evento_consumido_topic_partition_offset",
                        columnNames = {"topic", "partition_number", "offset_number"}
                )
        },
        indexes = {
                @Index(name = "idx_kafka_evento_consumido_topic", columnList = "topic"),
                @Index(name = "idx_kafka_evento_consumido_tipo", columnList = "event_type"),
                @Index(name = "idx_kafka_evento_consumido_estado", columnList = "estado_procesamiento")
        }
)
public class KafkaEventoConsumido extends BaseEntity {

    @Column(name = "source_service", nullable = false, length = 120)
    private String sourceService;

    @Column(name = "topic", nullable = false, length = 180)
    private String topic;

    @Column(name = "event_key", length = 180)
    private String eventKey;

    @Column(name = "partition_number")
    private Integer partition;

    @Column(name = "offset_number")
    private Long offset;

    @Column(name = "event_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 120)
    private String aggregateId;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Column(name = "producer", length = 120)
    private String producer;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_procesamiento", nullable = false, length = 30)
    private EstadoKafkaProcesamiento estadoProcesamiento;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;

    @Column(name = "last_error", columnDefinition = "nvarchar(max)")
    private String lastError;
}