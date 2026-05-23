// ruta: src/main/java/com/upsjb/ms4/dto/venta/filter/VentaFilterDto.java
package com.upsjb.ms4.dto.venta.filter;

import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record VentaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Size(max = 80, message = "El código de venta no debe superar 80 caracteres.")
        String codigoVenta,

        CanalVenta canalVenta,

        EstadoVenta estadoVenta,

        MetodoPago metodoPagoPrincipal,

        @Positive(message = "El idClienteSnapshot debe ser positivo.")
        Long idClienteSnapshot,

        @Positive(message = "El idClienteMs2 debe ser positivo.")
        Long idClienteMs2,

        @Positive(message = "El idUsuarioClienteMs1 debe ser positivo.")
        Long idUsuarioClienteMs1,

        @Positive(message = "El idEmpleadoSnapshot debe ser positivo.")
        Long idEmpleadoSnapshot,

        @Positive(message = "El idEmpleadoMs2 debe ser positivo.")
        Long idEmpleadoMs2,

        @Positive(message = "El idUsuarioEmpleadoMs1 debe ser positivo.")
        Long idUsuarioEmpleadoMs1,

        @Positive(message = "El idCaja debe ser positivo.")
        Long idCaja,

        Boolean estado,

        LocalDateTime fechaDesde,

        LocalDateTime fechaHasta
) {
}