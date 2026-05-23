package com.upsjb.ms4.domain.entity.snapshot;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "empleado_snapshot_ms2")
public class EmpleadoSnapshotMs2 extends BaseEntity {

    @Column(name = "id_empleado_ms2", nullable = false)
    private Long idEmpleadoMs2;

    @Column(name = "id_usuario_ms1", nullable = false)
    private Long idUsuarioMs1;

    @Column(name = "codigo_empleado", nullable = false, length = 80)
    private String codigoEmpleado;

    @Column(name = "empleado_activo_ms2", nullable = false)
    private Boolean empleadoActivoMs2;

    @Column(name = "id_area_ms2")
    private Long idAreaMs2;

    @Column(name = "area_codigo", length = 80)
    private String areaCodigo;

    @Column(name = "area_nombre", length = 180)
    private String areaNombre;

    @Column(name = "id_persona_ms2")
    private Long idPersonaMs2;

    @Column(name = "tipo_documento", length = 30)
    private String tipoDocumento;

    @Column(name = "numero_documento", length = 30)
    private String numeroDocumento;

    @Column(name = "nombres", length = 180)
    private String nombres;

    @Column(name = "ape_paterno", length = 120)
    private String apePaterno;

    @Column(name = "ape_materno", length = 120)
    private String apeMaterno;

    @Column(name = "nombre_completo", length = 300)
    private String nombreCompleto;

    @Column(name = "correo", length = 180)
    private String correo;

    @Column(name = "telefono_principal", length = 50)
    private String telefonoPrincipal;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_cese")
    private LocalDate fechaCese;

    @Column(name = "puede_crear")
    private Boolean puedeCrear;

    @Column(name = "puede_actualizar")
    private Boolean puedeActualizar;

    @Column(name = "persona_json", columnDefinition = "nvarchar(max)")
    private String personaJson;

    @Column(name = "event_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 120)
    private String aggregateId;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Column(name = "producer", length = 120)
    private String producer;

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