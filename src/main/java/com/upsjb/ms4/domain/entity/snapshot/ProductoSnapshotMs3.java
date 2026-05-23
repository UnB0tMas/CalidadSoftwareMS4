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
@Table(name = "producto_snapshot_ms3")
public class ProductoSnapshotMs3 extends BaseEntity {

    @Column(name = "id_producto_ms3", nullable = false)
    private Long idProductoMs3;

    @Column(name = "codigo_producto", nullable = false, length = 80)
    private String codigoProducto;

    @Column(name = "nombre", nullable = false, length = 250)
    private String nombre;

    @Column(name = "slug", length = 250)
    private String slug;

    @Column(name = "id_tipo_producto_ms3")
    private Long idTipoProductoMs3;

    @Column(name = "codigo_tipo_producto", length = 80)
    private String codigoTipoProducto;

    @Column(name = "nombre_tipo_producto", length = 180)
    private String nombreTipoProducto;

    @Column(name = "id_categoria_ms3")
    private Long idCategoriaMs3;

    @Column(name = "codigo_categoria", length = 80)
    private String codigoCategoria;

    @Column(name = "nombre_categoria", length = 180)
    private String nombreCategoria;

    @Column(name = "slug_categoria", length = 250)
    private String slugCategoria;

    @Column(name = "id_marca_ms3")
    private Long idMarcaMs3;

    @Column(name = "codigo_marca", length = 80)
    private String codigoMarca;

    @Column(name = "nombre_marca", length = 180)
    private String nombreMarca;

    @Column(name = "slug_marca", length = 250)
    private String slugMarca;

    @Column(name = "descripcion_corta", length = 500)
    private String descripcionCorta;

    @Column(name = "descripcion_larga", columnDefinition = "nvarchar(max)")
    private String descripcionLarga;

    @Column(name = "genero_objetivo", length = 80)
    private String generoObjetivo;

    @Column(name = "temporada", length = 80)
    private String temporada;

    @Column(name = "deporte", length = 120)
    private String deporte;

    @Column(name = "estado_registro", length = 60)
    private String estadoRegistro;

    @Column(name = "estado_publicacion", length = 60)
    private String estadoPublicacion;

    @Column(name = "estado_venta", length = 60)
    private String estadoVenta;

    @Column(name = "visible_publico", nullable = false)
    private Boolean visiblePublico;

    @Column(name = "vendible", nullable = false)
    private Boolean vendible;

    @Column(name = "fecha_publicacion_inicio")
    private LocalDateTime fechaPublicacionInicio;

    @Column(name = "fecha_publicacion_fin")
    private LocalDateTime fechaPublicacionFin;

    @Column(name = "motivo_estado", length = 500)
    private String motivoEstado;

    @Column(name = "atributos_json", columnDefinition = "nvarchar(max)")
    private String atributosJson;

    @Column(name = "skus_json", columnDefinition = "nvarchar(max)")
    private String skusJson;

    @Column(name = "imagenes_json", columnDefinition = "nvarchar(max)")
    private String imagenesJson;

    @Column(name = "event_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 120)
    private String aggregateId;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

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