// ruta: src/main/java/com/upsjb/ms4/dto/contingencia/filter/ModoContingenciaFilterDto.java
package com.upsjb.ms4.dto.contingencia.filter;

import com.upsjb.ms4.domain.enums.EstadoContingencia;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ModoContingenciaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Size(max = 40, message = "El servicio afectado no debe superar 40 caracteres.")
        String servicioAfectado,

        EstadoContingencia estadoContingencia,

        Boolean ventasPermitidas,

        Boolean guardarEventosPendientes,

        Boolean estado,

        LocalDateTime fechaInicioDesde,

        LocalDateTime fechaInicioHasta,

        LocalDateTime fechaFinDesde,

        LocalDateTime fechaFinHasta
) {
}