package com.upsjb.ms4.domain.entity.caja;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "caja")
public class Caja extends BaseEntity {

    @Column(name = "codigo_caja", nullable = false, unique = true, length = 80)
    private String codigoCaja;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_caja", nullable = false, length = 30)
    private EstadoCaja estadoCaja;

    @Column(name = "monto_inicial", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "monto_esperado_efectivo", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoEsperadoEfectivo;

    @Column(name = "monto_real_efectivo", precision = 18, scale = 2)
    private BigDecimal montoRealEfectivo;

    @Column(name = "monto_tarjeta", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoTarjeta;

    @Column(name = "monto_total_vendido", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoTotalVendido;

    @Column(name = "diferencia", precision = 18, scale = 2)
    private BigDecimal diferencia;

    @Column(name = "id_empleado_apertura_snapshot", nullable = false)
    private Long idEmpleadoAperturaSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado_apertura_snapshot", insertable = false, updatable = false)
    private EmpleadoSnapshotMs2 empleadoAperturaSnapshot;

    @Column(name = "id_usuario_apertura_ms1", nullable = false)
    private Long idUsuarioAperturaMs1;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "id_empleado_cierre_snapshot")
    private Long idEmpleadoCierreSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado_cierre_snapshot", insertable = false, updatable = false)
    private EmpleadoSnapshotMs2 empleadoCierreSnapshot;

    @Column(name = "id_usuario_cierre_ms1")
    private Long idUsuarioCierreMs1;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "observacion_apertura", length = 500)
    private String observacionApertura;

    @Column(name = "observacion_cierre", length = 500)
    private String observacionCierre;
}