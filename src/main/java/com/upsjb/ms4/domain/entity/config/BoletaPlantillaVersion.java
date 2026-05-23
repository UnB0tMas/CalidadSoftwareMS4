package com.upsjb.ms4.domain.entity.config;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "boleta_plantilla_version")
public class BoletaPlantillaVersion extends BaseEntity {

    @Column(name = "codigo_version", nullable = false, unique = true, length = 50)
    private String codigoVersion;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "ruta_template_html", nullable = false, length = 255)
    private String rutaTemplateHtml;

    @Column(name = "ruta_template_mail", nullable = false, length = 255)
    private String rutaTemplateMail;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDateTime fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDateTime fechaFinVigencia;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente;

    @Column(name = "creado_por_id_usuario_ms1", nullable = false)
    private Long creadoPorIdUsuarioMs1;

    @Column(name = "motivo", length = 500)
    private String motivo;
}