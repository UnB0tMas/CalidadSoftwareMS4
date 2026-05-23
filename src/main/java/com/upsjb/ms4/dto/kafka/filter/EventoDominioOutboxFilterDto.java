// ruta: src/main/java/com/upsjb/ms4/dto/kafka/filter/EventoDominioOutboxFilterDto.java
package com.upsjb.ms4.dto.kafka.filter;

import com.upsjb.ms4.domain.enums.EstadoOutbox;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventoDominioOutboxFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        UUID eventId,

        @Size(max = 80, message = "El aggregateType no debe superar 80 caracteres.")
        String aggregateType,

        @Size(max = 120, message = "El aggregateId no debe superar 120 caracteres.")
        String aggregateId,

        @Size(max = 180, message = "El topic no debe superar 180 caracteres.")
        String topic,

        @Size(max = 180, message = "El eventKey no debe superar 180 caracteres.")
        String eventKey,

        @Size(max = 120, message = "El eventType no debe superar 120 caracteres.")
        String eventType,

        EstadoOutbox status,

        @Min(value = 0, message = "El mínimo de intentos no puede ser negativo.")
        Integer attemptsMin,

        @Min(value = 0, message = "El máximo de intentos no puede ser negativo.")
        Integer attemptsMax,

        @Size(max = 120, message = "El lockedBy no debe superar 120 caracteres.")
        String lockedBy,

        Boolean bloqueado,

        Boolean conError,

        Boolean estado,

        LocalDateTime fechaCreacionDesde,

        LocalDateTime fechaCreacionHasta,

        LocalDateTime fechaPublicacionDesde,

        LocalDateTime fechaPublicacionHasta,

        LocalDateTime fechaBloqueoDesde,

        LocalDateTime fechaBloqueoHasta
) {
}