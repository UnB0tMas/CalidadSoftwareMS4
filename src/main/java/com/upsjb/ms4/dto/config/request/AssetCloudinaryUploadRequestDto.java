// ruta: src/main/java/com/upsjb/ms4/dto/config/request/AssetCloudinaryUploadRequestDto.java
package com.upsjb.ms4.dto.config.request;

import com.upsjb.ms4.domain.enums.ResourceTypeCloudinary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AssetCloudinaryUploadRequestDto(

        @NotBlank(message = "La entidad origen es obligatoria.")
        @Size(max = 80, message = "La entidad origen no debe superar 80 caracteres.")
        @Pattern(
                regexp = "^[A-Z0-9_]+$",
                message = "La entidad origen solo debe contener mayúsculas, números y guion bajo."
        )
        String entidadOrigen,

        @Positive(message = "El identificador de entidad origen debe ser positivo.")
        Long idEntidadOrigen,

        @NotBlank(message = "El tipo de asset es obligatorio.")
        @Size(max = 60, message = "El tipo de asset no debe superar 60 caracteres.")
        @Pattern(
                regexp = "^[A-Z0-9_]+$",
                message = "El tipo de asset solo debe contener mayúsculas, números y guion bajo."
        )
        String tipoAsset,

        @NotBlank(message = "La carpeta Cloudinary es obligatoria.")
        @Size(max = 255, message = "La carpeta no debe superar 255 caracteres.")
        @Pattern(
                regexp = "^[a-zA-Z0-9/_-]+$",
                message = "La carpeta solo puede contener letras, números, slash, guion y guion bajo."
        )
        String folder,

        @NotNull(message = "El resource type es obligatorio.")
        ResourceTypeCloudinary resourceType
) {
}