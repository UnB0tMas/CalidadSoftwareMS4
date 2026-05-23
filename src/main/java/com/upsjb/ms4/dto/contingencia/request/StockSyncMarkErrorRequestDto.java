// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/request/StockSyncMarkErrorRequestDto.java
package com.upsjb.ms4.dto.contingencia.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StockSyncMarkErrorRequestDto(

        @NotBlank(message = "El detalle de error es obligatorio.")
        @Size(max = 4000, message = "El detalle de error no debe superar 4000 caracteres.")
        String errorDetalle
) {
}