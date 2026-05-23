package com.upsjb.ms4.domain.entity.boleta;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import com.upsjb.ms4.domain.entity.config.ConfiguracionEmpresaVersion;
import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoBoleta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "boleta")
public class Boleta extends BaseEntity {

    @Column(name = "id_venta", nullable = false, unique = true)
    private Long idVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", insertable = false, updatable = false)
    private Venta venta;

    @Column(name = "id_serie_boleta", nullable = false)
    private Long idSerieBoleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serie_boleta", insertable = false, updatable = false)
    private SerieBoleta serieBoleta;

    @Column(name = "id_configuracion_empresa_version", nullable = false)
    private Long idConfiguracionEmpresaVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_configuracion_empresa_version", insertable = false, updatable = false)
    private ConfiguracionEmpresaVersion configuracionEmpresaVersion;

    @Column(name = "id_configuracion_tributaria_version", nullable = false)
    private Long idConfiguracionTributariaVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_configuracion_tributaria_version", insertable = false, updatable = false)
    private ConfiguracionTributariaVersion configuracionTributariaVersion;

    @Column(name = "id_boleta_plantilla_version", nullable = false)
    private Long idBoletaPlantillaVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boleta_plantilla_version", insertable = false, updatable = false)
    private BoletaPlantillaVersion boletaPlantillaVersion;

    @Column(name = "serie", nullable = false, length = 20)
    private String serie;

    @Column(name = "numero", nullable = false)
    private Long numero;

    @Column(name = "codigo_boleta", nullable = false, unique = true, length = 80)
    private String codigoBoleta;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda;

    @Column(name = "ruc_emisor", nullable = false, length = 20)
    private String rucEmisor;

    @Column(name = "razon_social_emisor", nullable = false, length = 250)
    private String razonSocialEmisor;

    @Column(name = "nombre_comercial_emisor", length = 250)
    private String nombreComercialEmisor;

    @Column(name = "direccion_fiscal_emisor", nullable = false, length = 500)
    private String direccionFiscalEmisor;

    @Column(name = "telefono_emisor", length = 50)
    private String telefonoEmisor;

    @Column(name = "correo_emisor", length = 180)
    private String correoEmisor;

    @Column(name = "logo_url_emisor", length = 1000)
    private String logoUrlEmisor;

    @Column(name = "tipo_documento_cliente", length = 30)
    private String tipoDocumentoCliente;

    @Column(name = "numero_documento_cliente", length = 30)
    private String numeroDocumentoCliente;

    @Column(name = "nombre_cliente", nullable = false, length = 300)
    private String nombreCliente;

    @Column(name = "correo_cliente", length = 180)
    private String correoCliente;

    @Column(name = "telefono_cliente", length = 50)
    private String telefonoCliente;

    @Column(name = "direccion_cliente", length = 500)
    private String direccionCliente;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "descuento_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal descuentoTotal;

    @Column(name = "op_gravada", nullable = false, precision = 18, scale = 2)
    private BigDecimal opGravada;

    @Column(name = "op_exonerada", nullable = false, precision = 18, scale = 2)
    private BigDecimal opExonerada;

    @Column(name = "op_inafecta", nullable = false, precision = 18, scale = 2)
    private BigDecimal opInafecta;

    @Column(name = "igv_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal igvPorcentaje;

    @Column(name = "igv_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal igvTotal;

    @Column(name = "total", nullable = false, precision = 18, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_boleta", nullable = false, length = 40)
    private EstadoBoleta estadoBoleta;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;

    @Column(name = "hash_payload", nullable = false, length = 128)
    private String hashPayload;

    @Column(name = "version_plantilla", nullable = false, length = 50)
    private String versionPlantilla;

    @Column(name = "enviado_por_correo", nullable = false)
    private Boolean enviadoPorCorreo;

    @Column(name = "fecha_ultimo_envio_correo")
    private LocalDateTime fechaUltimoEnvioCorreo;

    @Column(name = "cantidad_envios_correo", nullable = false)
    private Integer cantidadEnviosCorreo;
}