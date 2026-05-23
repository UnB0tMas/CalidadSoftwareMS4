package com.upsjb.ms4.domain.entity.pago;

import com.upsjb.ms4.domain.entity.base.BaseEntity;
import com.upsjb.ms4.domain.enums.EstadoKafkaProcesamiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "stripe_evento")
public class StripeEvento extends BaseEntity {

    @Column(name = "stripe_event_id", nullable = false, unique = true, length = 120)
    private String stripeEventId;

    @Column(name = "stripe_event_type", nullable = false, length = 120)
    private String stripeEventType;

    @Column(name = "stripe_payment_intent_id", length = 120)
    private String stripePaymentIntentId;

    @Column(name = "id_pago")
    private Long idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago", insertable = false, updatable = false)
    private Pago pago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_procesamiento", nullable = false, length = 40)
    private EstadoKafkaProcesamiento estadoProcesamiento;

    @Column(name = "payload_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String payloadJson;

    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "error_detalle", columnDefinition = "nvarchar(max)")
    private String errorDetalle;
}