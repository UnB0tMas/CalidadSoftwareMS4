package com.upsjb.ms4.domain.entity.config;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "configuracion_empresa_version")
public class ConfiguracionEmpresaVersion extends BaseEntity {

    @Column(name = "codigo_version", nullable = false, unique = true, length = 50)
    private String codigoVersion;

    @Column(name = "ruc", nullable = false, length = 20)
    private String ruc;

    @Column(name = "razon_social", nullable = false, length = 250)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 250)
    private String nombreComercial;

    @Column(name = "direccion_fiscal", nullable = false, length = 500)
    private String direccionFiscal;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "correo", length = 180)
    private String correo;

    @Column(name = "web", length = 250)
    private String web;

    @Column(name = "id_logo_asset")
    private Long idLogoAsset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_logo_asset", insertable = false, updatable = false)
    private AssetCloudinary logoAsset;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "logo_public_id", length = 500)
    private String logoPublicId;

    @Column(name = "color_primario", length = 20)
    private String colorPrimario;

    @Column(name = "color_secundario", length = 20)
    private String colorSecundario;

    @Column(name = "mensaje_pie_boleta", length = 1000)
    private String mensajePieBoleta;

    @Column(name = "terminos_condiciones", columnDefinition = "nvarchar(max)")
    private String terminosCondiciones;

    @Column(name = "politica_cambios", columnDefinition = "nvarchar(max)")
    private String politicaCambios;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDateTime fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDateTime fechaFinVigencia;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "modificado_por_id_usuario_ms1", nullable = false)
    private Long modificadoPorIdUsuarioMs1;
}