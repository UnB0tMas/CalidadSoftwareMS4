// ruta: src/main/java/com/upsjb/ms4/dto/config/filter/AssetCloudinaryFilterDto.java
package com.upsjb.ms4.dto.config.filter;

import com.upsjb.ms4.domain.enums.ResourceTypeCloudinary;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AssetCloudinaryFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Size(max = 80, message = "La entidad origen no debe superar 80 caracteres.")
        @Pattern(
                regexp = "^[A-Z0-9_]+$",
                message = "La entidad origen solo debe contener mayúsculas, números y guion bajo."
        )
        String entidadOrigen,

        @Positive(message = "El identificador de entidad origen debe ser positivo.")
        Long idEntidadOrigen,

        @Size(max = 60, message = "El tipo de asset no debe superar 60 caracteres.")
        @Pattern(
                regexp = "^[A-Z0-9_]+$",
                message = "El tipo de asset solo debe contener mayúsculas, números y guion bajo."
        )
        String tipoAsset,

        ResourceTypeCloudinary resourceType,

        @Size(max = 255, message = "La carpeta no debe superar 255 caracteres.")
        String folder,

        Boolean estado
) {
}