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
@Table(name = "cliente_snapshot_ms2")
public class ClienteSnapshotMs2 extends BaseEntity {

    @Column(name = "id_cliente_ms2", nullable = false)
    private Long idClienteMs2;

    @Column(name = "id_usuario_ms1", nullable = false)
    private Long idUsuarioMs1;

    @Column(name = "tipo_cliente", nullable = false, length = 30)
    private String tipoCliente;

    @Column(name = "cliente_activo_ms2", nullable = false)
    private Boolean clienteActivoMs2;

    @Column(name = "id_persona_ms2")
    private Long idPersonaMs2;

    @Column(name = "tipo_documento_persona", length = 30)
    private String tipoDocumentoPersona;

    @Column(name = "numero_documento_persona", length = 30)
    private String numeroDocumentoPersona;

    @Column(name = "nombres", length = 180)
    private String nombres;

    @Column(name = "ape_paterno", length = 120)
    private String apePaterno;

    @Column(name = "ape_materno", length = 120)
    private String apeMaterno;

    @Column(name = "nombre_completo", length = 300)
    private String nombreCompleto;

    @Column(name = "id_empresa_ms2")
    private Long idEmpresaMs2;

    @Column(name = "ruc", length = 20)
    private String ruc;

    @Column(name = "razon_social", length = 250)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 250)
    private String nombreComercial;

    @Column(name = "correo_principal", length = 180)
    private String correoPrincipal;

    @Column(name = "telefono_principal", length = 50)
    private String telefonoPrincipal;

    @Column(name = "direccion_principal", length = 500)
    private String direccionPrincipal;

    @Column(name = "referencia_direccion", length = 500)
    private String referenciaDireccion;

    @Column(name = "ubigeo", length = 20)
    private String ubigeo;

    @Column(name = "distrito", length = 120)
    private String distrito;

    @Column(name = "provincia", length = 120)
    private String provincia;

    @Column(name = "departamento", length = 120)
    private String departamento;

    @Column(name = "persona_json", columnDefinition = "nvarchar(max)")
    private String personaJson;

    @Column(name = "empresa_json", columnDefinition = "nvarchar(max)")
    private String empresaJson;

    @Column(name = "telefonos_json", columnDefinition = "nvarchar(max)")
    private String telefonosJson;

    @Column(name = "direcciones_json", columnDefinition = "nvarchar(max)")
    private String direccionesJson;

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