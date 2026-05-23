// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/filter/InventarioEventoPendienteFilterDto.java
package com.upsjb.ms4.dto.contingencia.filter;

import com.upsjb.ms4.domain.enums.EstadoSincronizacionInventario;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record InventarioEventoPendienteFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        Long idVenta,

        @Size(max = 80, message = "El código de venta no debe superar 80 caracteres.")
        String codigoVenta,

        Long idVentaDetalle,

        TipoComandoStock tipoEvento,

        @Size(max = 180, message = "El topic destino no debe superar 180 caracteres.")
        String topicDestino,

        EstadoSincronizacionInventario estadoSincronizacion,

        @Size(max = 180, message = "La clave de idempotencia no debe superar 180 caracteres.")
        String idempotencyKey,

        Boolean estado,

        LocalDateTime fechaCreacionDesde,

        LocalDateTime fechaCreacionHasta,

        LocalDateTime fechaSincronizacionDesde,

        LocalDateTime fechaSincronizacionHasta
) {
}