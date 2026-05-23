package com.upsjb.ms4.dto.contingencia.request;

import jakarta.validation.constraints.Size;

public record StockSyncMarkSyncedRequestDto(

        @Size(max = 2000, message = "El detalle del resultado no debe superar 2000 caracteres.")
        String detalleResultado
) {
}