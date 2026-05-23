package com.upsjb.ms4.domain.entity.config;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.enums.NombreImpuesto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "configuracion_tributaria_version")
public class ConfiguracionTributariaVersion extends BaseEntity {

    @Column(name = "codigo_version", nullable = false, unique = true, length = 50)
    private String codigoVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre_impuesto", nullable = false, length = 50)
    private NombreImpuesto nombreImpuesto;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDateTime fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDateTime fechaFinVigencia;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "modificado_por_id_usuario_ms1", nullable = false)
    private Long modificadoPorIdUsuarioMs1;
}