// ruta: src/main/java/com/upsjb/ms4/dto/boleta/filter/BoletaFilterDto.java
package com.upsjb.ms4.dto.boleta.filter;

import com.upsjb.ms4.domain.enums.EstadoBoleta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BoletaFilterDto(

        @Size(max = 150, message = "El texto de búsqueda no debe superar 150 caracteres.")
        String search,

        @Positive(message = "El id de venta debe ser positivo.")
        Long idVenta,

        @Pattern(regexp = "^B\\d{3}$", message = "La serie debe tener formato B001.")
        String serie,

        @Positive(message = "El número de boleta debe ser positivo.")
        Long numero,

        @Size(max = 80, message = "El código de boleta no debe superar 80 caracteres.")
        @Pattern(
                regexp = "^[A-Z0-9]{1,10}-\\d{1,12}$",
                message = "El código de boleta debe tener un formato válido, por ejemplo B001-00000001."
        )
        String codigoBoleta,

        @Size(max = 30, message = "El número de documento del cliente no debe superar 30 caracteres.")
        String numeroDocumentoCliente,

        @Email(message = "El correo del cliente debe tener formato válido.")
        @Size(max = 180, message = "El correo del cliente no debe superar 180 caracteres.")
        String correoCliente,

        EstadoBoleta estadoBoleta,

        Boolean enviadoPorCorreo,

        Boolean estado,

        LocalDateTime fechaDesde,

        LocalDateTime fechaHasta
) {
}