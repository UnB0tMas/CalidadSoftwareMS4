package com.upsjb.ms4.domain.entity.snapshot;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "promocion_snapshot_ms3")
public class PromocionSnapshotMs3 extends BaseEntity {

    @Column(name = "id_promocion_ms3", nullable = false)
    private Long idPromocionMs3;

    @Column(name = "codigo_promocion", nullable = false, length = 80)
    private String codigoPromocion;

    @Column(name = "nombre", nullable = false, length = 180)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "creado_por_id_usuario_ms1")
    private Long creadoPorIdUsuarioMs1;

    @Column(name = "id_promocion_version_ms3", nullable = false)
    private Long idPromocionVersionMs3;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "estado_promocion", length = 60)
    private String estadoPromocion;

    @Column(name = "visible_publico", nullable = false)
    private Boolean visiblePublico;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "descuentos_json", columnDefinition = "nvarchar(max)")
    private String descuentosJson;

    @Column(name = "event_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 120)
    private String aggregateId;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;

    @Column(name = "fecha_sincronizacion", nullable = false)
    private LocalDateTime fechaSincronizacion;
}