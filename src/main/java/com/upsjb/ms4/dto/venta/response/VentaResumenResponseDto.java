// ruta: src/main/java/com/upsjb/ms4/dto/venta/response/VentaResumenResponseDto.java
package com.upsjb.ms4.dto.venta.response;

import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResumenResponseDto(
        Long id,
        String codigoVenta,
        CanalVenta canalVenta,
        EstadoVenta estadoVenta,
        String clienteNombre,
        String empleadoNombre,
        MetodoPago metodoPagoPrincipal,
        String moneda,
        BigDecimal total,
        LocalDateTime fechaVenta
) {
}