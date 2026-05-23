// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/request/StockSyncResultRequestDto.java
package com.upsjb.ms4.dto.contingencia.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockSyncResultRequestDto(

        @NotNull(message = "El id del evento pendiente es obligatorio.")
        @Positive(message = "El id del evento pendiente debe ser positivo.")
        Long idEventoPendiente,

        @NotNull(message = "Debe indicar si el evento fue sincronizado.")
        Boolean sincronizado,

        @Size(max = 2000, message = "El detalle del resultado no debe superar 2000 caracteres.")
        String detalleResultado,

        @Size(max = 4000, message = "El detalle de error no debe superar 4000 caracteres.")
        String errorDetalle
) {
}