// ruta: src/main/java/com/upsjb/ms4/domain/entity/kafka/EventoDominioOutbox.java
package com.upsjb.ms4.domain.entity.kafka;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.enums.EstadoOutbox;
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
        name = "evento_dominio_outbox",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_evento_dominio_outbox_event_id", columnNames = "event_id")
        },
        indexes = {
                @Index(name = "idx_evento_dominio_outbox_status", columnList = "status"),
                @Index(name = "idx_evento_dominio_outbox_topic", columnList = "topic"),
                @Index(name = "idx_evento_dominio_outbox_aggregate", columnList = "aggregate_type, aggregate_id"),
                @Index(name = "idx_evento_dominio_outbox_locked_at", columnList = "locked_at")
        }
)
public class EventoDominioOutbox extends BaseEntity {

    @Column(name = "event_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID eventId;

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 120)
    private String aggregateId;

    @Column(name = "topic", nullable = false, length = 180)
    private String topic;

    @Column(name = "event_key", length = 180)
    private String eventKey;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EstadoOutbox status;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts;

    @Column(name = "last_error", columnDefinition = "nvarchar(max)")
    private String lastError;

    @Column(name = "locked_by", length = 120)
    private String lockedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}