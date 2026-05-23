// ruta: src/main/java/com/upsjb/ms4/dto/config/request/ConfiguracionEmpresaRequestDto.java
package com.upsjb.ms4.dto.config.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ConfiguracionEmpresaRequestDto(

        @NotBlank(message = "El RUC es obligatorio.")
        @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener 11 dígitos.")
        String ruc,

        @NotBlank(message = "La razón social es obligatoria.")
        @Size(max = 250, message = "La razón social no debe superar 250 caracteres.")
        String razonSocial,

        @Size(max = 250, message = "El nombre comercial no debe superar 250 caracteres.")
        String nombreComercial,

        @NotBlank(message = "La dirección fiscal es obligatoria.")
        @Size(max = 500, message = "La dirección fiscal no debe superar 500 caracteres.")
        String direccionFiscal,

        @Size(max = 50, message = "El teléfono no debe superar 50 caracteres.")
        String telefono,

        @Email(message = "El correo debe tener formato válido.")
        @Size(max = 180, message = "El correo no debe superar 180 caracteres.")
        String correo,

        @Size(max = 250, message = "La web no debe superar 250 caracteres.")
        String web,

        @Positive(message = "El asset de logo debe ser un identificador positivo.")
        Long idLogoAsset,

        @Pattern(
                regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "El color primario debe tener formato hexadecimal, por ejemplo #0D6EFD."
        )
        @Size(max = 20, message = "El color primario no debe superar 20 caracteres.")
        String colorPrimario,

        @Pattern(
                regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "El color secundario debe tener formato hexadecimal, por ejemplo #6C757D."
        )
        @Size(max = 20, message = "El color secundario no debe superar 20 caracteres.")
        String colorSecundario,

        @Size(max = 1000, message = "El mensaje de pie de boleta no debe superar 1000 caracteres.")
        String mensajePieBoleta,

        @Size(max = 10000, message = "Los términos y condiciones no deben superar 10000 caracteres.")
        String terminosCondiciones,

        @Size(max = 10000, message = "La política de cambios no debe superar 10000 caracteres.")
        String politicaCambios,

        LocalDateTime fechaInicioVigencia,

        @NotBlank(message = "El motivo de la nueva versión empresarial es obligatorio.")
        @Size(max = 500, message = "El motivo no debe superar 500 caracteres.")
        String motivo
) {
}