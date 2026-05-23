package com.upsjb.ms4.domain.entity.auditoria;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.enums.ResultadoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@Table(name = "auditoria_funcional")
public class AuditoriaFuncional extends BaseEntity {

    @Column(name = "entidad", nullable = false, length = 80)
    private String entidad;

    @Column(name = "entidad_id", length = 120)
    private String entidadId;

    @Column(name = "accion", nullable = false, length = 100)
    private String accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 40)
    private ResultadoAuditoria resultado;

    @Column(name = "actor_id_usuario_ms1")
    private Long actorIdUsuarioMs1;

    @Column(name = "actor_rol", length = 40)
    private String actorRol;

    @Column(name = "actor_username", length = 180)
    private String actorUsername;

    @Column(name = "ip", length = 80)
    private String ip;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "detalle_json", columnDefinition = "nvarchar(max)")
    private String detalleJson;
}