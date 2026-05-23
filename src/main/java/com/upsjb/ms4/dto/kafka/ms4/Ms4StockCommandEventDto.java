// ruta: src/main/java/com/upsjb/ms4/dto/kafka/ms4/Ms4StockCommandEventDto.java
package com.upsjb.ms4.dto.kafka.ms4;

import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;

public record Ms4StockCommandEventDto(
        DomainEventEnvelopeDto<Ms4StockCommandPayloadDto> envelope
) {

    public Ms4StockCommandPayloadDto payload() {
        return envelope == null ? null : envelope.payload();
    }
}