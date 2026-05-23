// ruta: src/main/java/com/upsjb/ms4/dto/snapshot/filter/PromocionVentaFilterDto.java
package com.upsjb.ms4.dto.snapshot.filter;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PromocionVentaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Positive(message = "El idPromocionMs3 debe ser positivo.")
        Long idPromocionMs3,

        @Positive(message = "El idPromocionVersionMs3 debe ser positivo.")
        Long idPromocionVersionMs3,

        @Size(max = 80, message = "El código de promoción no debe superar 80 caracteres.")
        String codigoPromocion,

        @Size(max = 60, message = "El estado de promoción no debe superar 60 caracteres.")
        String estadoPromocion,

        Boolean visiblePublico,

        Boolean vigente,

        Boolean estado,

        LocalDateTime fechaDesde,

        LocalDateTime fechaHasta
) {
}