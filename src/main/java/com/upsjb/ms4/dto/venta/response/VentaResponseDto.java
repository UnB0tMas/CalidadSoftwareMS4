// ruta: src/main/java/com/upsjb/ms4/dto/venta/response/VentaResponseDto.java
package com.upsjb.ms4.dto.venta.response;

import com.upsjb.ms4.domain.enums.CanalVenta;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResponseDto(
        Long id,
        String codigoVenta,
        CanalVenta canalVenta,
        EstadoVenta estadoVenta,
        Long idClienteSnapshot,
        Long idClienteMs2,
        Long idUsuarioClienteMs1,
        String clienteNombre,
        String clienteDocumento,
        Long idEmpleadoSnapshot,
        Long idEmpleadoMs2,
        Long idUsuarioEmpleadoMs1,
        String empleadoNombre,
        Long idCaja,
        String codigoCaja,
        Long idConfiguracionTributariaVersion,
        String moneda,
        BigDecimal subtotal,
        BigDecimal descuentoTotal,
        BigDecimal opGravada,
        BigDecimal opExonerada,
        BigDecimal opInafecta,
        BigDecimal igvPorcentaje,
        BigDecimal igvTotal,
        BigDecimal total,
        MetodoPago metodoPagoPrincipal,
        LocalDateTime fechaVenta,
        String observacion,
        String requestId,
        String correlationId,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}