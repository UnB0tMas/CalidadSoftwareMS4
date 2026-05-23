package com.upsjb.ms4.domain.entity.venta;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
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
@Table(name = "venta")
public class Venta extends BaseEntity {

    @Column(name = "codigo_venta", nullable = false, unique = true, length = 80)
    private String codigoVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_venta", nullable = false, length = 30)
    private CanalVenta canalVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_venta", nullable = false, length = 40)
    private EstadoVenta estadoVenta;

    @Column(name = "id_cliente_snapshot", nullable = false)
    private Long idClienteSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente_snapshot", insertable = false, updatable = false)
    private ClienteSnapshotMs2 clienteSnapshot;

    @Column(name = "id_cliente_ms2", nullable = false)
    private Long idClienteMs2;

    @Column(name = "id_usuario_cliente_ms1")
    private Long idUsuarioClienteMs1;

    @Column(name = "id_empleado_snapshot")
    private Long idEmpleadoSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado_snapshot", insertable = false, updatable = false)
    private EmpleadoSnapshotMs2 empleadoSnapshot;

    @Column(name = "id_empleado_ms2")
    private Long idEmpleadoMs2;

    @Column(name = "id_usuario_empleado_ms1")
    private Long idUsuarioEmpleadoMs1;

    @Column(name = "id_caja")
    private Long idCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", insertable = false, updatable = false)
    private Caja caja;

    @Column(name = "id_configuracion_tributaria_version", nullable = false)
    private Long idConfiguracionTributariaVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_configuracion_tributaria_version", insertable = false, updatable = false)
    private ConfiguracionTributariaVersion configuracionTributariaVersion;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda;

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
    @Column(name = "metodo_pago_principal", length = 60)
    private MetodoPago metodoPagoPrincipal;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}