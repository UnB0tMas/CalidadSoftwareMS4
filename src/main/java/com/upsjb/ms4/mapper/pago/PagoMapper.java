// ruta: src/main/java/com/upsjb/ms4/mapper/pago/PagoMapper.java
package com.upsjb.ms4.mapper.pago;

import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.dto.pago.response.PagoResponseDto;
import com.upsjb.ms4.dto.pago.response.StripePaymentIntentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public PagoResponseDto toResponse(Pago entity) {
        if (entity == null) return null;

        return new PagoResponseDto(
                entity.getId(),
                entity.getIdVenta(),
                entity.getCodigoPago(),
                entity.getMetodoPago(),
                entity.getEstadoPago(),
                entity.getMoneda(),
                entity.getMonto(),
                entity.getStripePaymentIntentId(),
                entity.getStripeChargeId(),
                entity.getStripeStatus(),
                entity.getFechaPago(),
                entity.getFechaConfirmacion(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PagoResponseDto toDetailResponse(Pago entity) {
        return toResponse(entity);
    }

    public StripePaymentIntentResponseDto toPaymentIntentResponse(
            Pago entity,
            String clientSecret,
            String publishableKey
    ) {
        if (entity == null) return null;

        return new StripePaymentIntentResponseDto(
                entity.getIdVenta(),
                entity.getId(),
                entity.getCodigoPago(),
                entity.getMetodoPago(),
                entity.getStripePaymentIntentId(),
                clientSecret,
                publishableKey,
                entity.getStripeStatus(),
                entity.getMoneda(),
                entity.getMonto()
        );
    }
}