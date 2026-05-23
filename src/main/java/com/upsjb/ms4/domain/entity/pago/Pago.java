package com.upsjb.ms4.domain.entity.pago;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoPago;
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
@Table(name = "pago")
public class Pago extends BaseEntity {

    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", insertable = false, updatable = false)
    private Venta venta;

    @Column(name = "codigo_pago", nullable = false, unique = true, length = 80)
    private String codigoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 60)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 40)
    private EstadoPago estadoPago;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda;

    @Column(name = "monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(name = "stripe_payment_intent_id", length = 120)
    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id", length = 120)
    private String stripeChargeId;

    @Column(name = "stripe_status", length = 80)
    private String stripeStatus;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "payload_pasarela_json", columnDefinition = "nvarchar(max)")
    private String payloadPasarelaJson;
}