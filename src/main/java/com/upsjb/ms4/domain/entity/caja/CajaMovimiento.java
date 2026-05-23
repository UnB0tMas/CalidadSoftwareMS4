package com.upsjb.ms4.domain.entity.caja;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.TipoMovimientoCaja;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "caja_movimiento")
public class CajaMovimiento extends BaseEntity {

    @Column(name = "id_caja", nullable = false)
    private Long idCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", insertable = false, updatable = false)
    private Caja caja;

    @Column(name = "id_venta")
    private Long idVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", insertable = false, updatable = false)
    private Venta venta;

    @Column(name = "id_pago")
    private Long idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago", insertable = false, updatable = false)
    private Pago pago;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 40)
    private TipoMovimientoCaja tipoMovimiento;

    @Column(name = "monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "actor_id_usuario_ms1", nullable = false)
    private Long actorIdUsuarioMs1;

    @Column(name = "actor_rol", nullable = false, length = 40)
    private String actorRol;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}