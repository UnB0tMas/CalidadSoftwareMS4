package com.upsjb.ms4.domain.entity.config;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "serie_boleta")
public class SerieBoleta extends BaseEntity {

    @Column(name = "serie", nullable = false, unique = true, length = 20)
    private String serie;

    @Column(name = "numero_actual", nullable = false)
    private Long numeroActual;

    @Column(name = "numero_inicio", nullable = false)
    private Long numeroInicio;

    @Column(name = "numero_fin")
    private Long numeroFin;

    @Column(name = "creado_por_id_usuario_ms1", nullable = false)
    private Long creadoPorIdUsuarioMs1;
}