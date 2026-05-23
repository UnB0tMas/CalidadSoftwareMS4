// ruta: src/main/java/com/upsjb/ms4/dto/config/response/BoletaPlantillaResponseDto.java
package com.upsjb.ms4.dto.config.response;

import java.time.LocalDateTime;

public record BoletaPlantillaResponseDto(
        Long id,
        String codigoVersion,
        String nombre,
        String rutaTemplateHtml,
        String rutaTemplateMail,
        String descripcion,
        LocalDateTime fechaInicioVigencia,
        LocalDateTime fechaFinVigencia,
        Boolean vigente,
        Long creadoPorIdUsuarioMs1,
        String motivo,
        Boolean estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}