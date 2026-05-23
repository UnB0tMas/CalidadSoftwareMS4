package com.upsjb.ms4.domain.entity.mail;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.enums.EstadoCorreo;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "correo_outbox")
public class CorreoOutbox extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true, columnDefinition = "uniqueidentifier")
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_correo", nullable = false, length = 80)
    private TipoCorreo tipoCorreo;

    @Column(name = "entidad_origen", nullable = false, length = 80)
    private String entidadOrigen;

    @Column(name = "id_entidad_origen", nullable = false)
    private Long idEntidadOrigen;

    @Column(name = "id_boleta")
    private Long idBoleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boleta", insertable = false, updatable = false)
    private Boleta boleta;

    @Column(name = "destinatario_email", nullable = false, length = 180)
    private String destinatarioEmail;

    @Column(name = "destinatario_nombre", length = 250)
    private String destinatarioNombre;

    @Column(name = "asunto", nullable = false, length = 250)
    private String asunto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_correo", nullable = false, length = 30)
    private EstadoCorreo estadoCorreo;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts;

    @Column(name = "last_error", columnDefinition = "nvarchar(max)")
    private String lastError;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}