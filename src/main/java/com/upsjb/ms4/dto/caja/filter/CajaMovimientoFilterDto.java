// ruta: src/main/java/com/upsjb/ms4/dto/caja/filter/CajaMovimientoFilterDto.java
package com.upsjb.ms4.dto.caja.filter;

import com.upsjb.ms4.domain.enums.TipoMovimientoCaja;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CajaMovimientoFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Positive(message = "La caja debe ser un identificador positivo.")
        Long idCaja,

        @Positive(message = "La venta debe ser un identificador positivo.")
        Long idVenta,

        @Positive(message = "El pago debe ser un identificador positivo.")
        Long idPago,

        TipoMovimientoCaja tipoMovimiento,

        @Positive(message = "El actor debe ser un identificador positivo.")
        Long actorIdUsuarioMs1,

        @Size(max = 40, message = "El rol del actor no debe superar 40 caracteres.")
        String actorRol,

        Boolean estado,

        LocalDateTime fechaDesde,

        LocalDateTime fechaHasta
) {
}