package com.upsjb.ms4.domain.entity.contingencia;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.enums.EstadoContingencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "modo_contingencia")
public class ModoContingencia extends BaseEntity {

    @Column(name = "servicio_afectado", nullable = false, length = 40)
    private String servicioAfectado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_contingencia", nullable = false, length = 40)
    private EstadoContingencia estadoContingencia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "activado_por_id_usuario_ms1")
    private Long activadoPorIdUsuarioMs1;

    @Column(name = "activado_por_rol", length = 40)
    private String activadoPorRol;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "ventas_permitidas", nullable = false)
    private Boolean ventasPermitidas;

    @Column(name = "guardar_eventos_pendientes", nullable = false)
    private Boolean guardarEventosPendientes;

    @Column(name = "total_eventos_pendientes", nullable = false)
    private Integer totalEventosPendientes;

    @Column(name = "observacion", length = 500)
    private String observacion;
}