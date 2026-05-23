// ruta: src/main/java/com/upsjb/ms4/dto/venta/response/VentaDetailResponseDto.java
package com.upsjb.ms4.dto.venta.response;

import com.upsjb.ms4.dto.boleta.response.BoletaResponseDto;
import com.upsjb.ms4.dto.pago.response.PagoResponseDto;
import java.util.List;

public record VentaDetailResponseDto(
        VentaResponseDto venta,
        List<VentaDetalleResponseDto> detalles,
        List<PagoResponseDto> pagos,
        BoletaResponseDto boleta
) {
}