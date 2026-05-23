// ruta: src/main/java/com/upsjb/ms4/dto/config/request/BoletaPlantillaRequestDto.java
package com.upsjb.ms4.dto.config.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BoletaPlantillaRequestDto(

        @NotBlank(message = "El nombre de la plantilla es obligatorio.")
        @Size(max = 150, message = "El nombre no debe superar 150 caracteres.")
        String nombre,

        @NotBlank(message = "La ruta del template HTML de boleta es obligatoria.")
        @Size(max = 255, message = "La ruta del template HTML no debe superar 255 caracteres.")
        String rutaTemplateHtml,

        @NotBlank(message = "La ruta del template de correo es obligatoria.")
        @Size(max = 255, message = "La ruta del template de correo no debe superar 255 caracteres.")
        String rutaTemplateMail,

        @Size(max = 500, message = "La descripción no debe superar 500 caracteres.")
        String descripcion,

        LocalDateTime fechaInicioVigencia,

        LocalDateTime fechaFinVigencia,

        @NotNull(message = "Debe indicar si la plantilla será vigente.")
        Boolean vigente,

        @NotBlank(message = "El motivo es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}