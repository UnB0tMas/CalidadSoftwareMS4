// ruta: src/main/java/com/upsjb/ms4/domain/entity/kafka/InventarioEventoPendienteMs4.java
package com.upsjb.ms4.domain.entity.kafka;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.EstadoSincronizacionInventario;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "inventario_evento_pendiente_ms4",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventario_evento_pendiente_idempotency",
                        columnNames = "idempotency_key"
                )
        },
        indexes = {
                @Index(name = "idx_inv_evt_pendiente_estado", columnList = "estado_sincronizacion"),
                @Index(name = "idx_inv_evt_pendiente_venta", columnList = "id_venta"),
                @Index(name = "idx_inv_evt_pendiente_codigo_venta", columnList = "codigo_venta"),
                @Index(name = "idx_inv_evt_pendiente_tipo", columnList = "tipo_evento")
        }
)
public class InventarioEventoPendienteMs4 extends BaseEntity {

    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", insertable = false, updatable = false)
    private Venta venta;

    @Column(name = "codigo_venta", nullable = false, length = 80)
    private String codigoVenta;

    @Column(name = "id_venta_detalle")
    private Long idVentaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta_detalle", insertable = false, updatable = false)
    private VentaDetalle ventaDetalle;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 60)
    private TipoComandoStock tipoEvento;

    @Column(name = "topic_destino", nullable = false, length = 180)
    private String topicDestino;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sincronizacion", nullable = false, length = 40)
    private EstadoSincronizacionInventario estadoSincronizacion;

    @Column(name = "idempotency_key", nullable = false, length = 180)
    private String idempotencyKey;

    @Column(name = "cantidad_reintentos", nullable = false)
    private Integer cantidadReintentos;

    @Column(name = "ultimo_error", columnDefinition = "nvarchar(max)")
    private String ultimoError;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultimo_reintento")
    private LocalDateTime fechaUltimoReintento;

    @Column(name = "fecha_sincronizacion")
    private LocalDateTime fechaSincronizacion;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}