// ruta: src/main/java/com/upsjb/ms4/dto/shared/IdResponseDto.java
package com.upsjb.ms4.dto.shared;

public record IdResponseDto(
        Long id,
        String codigo,
        String mensaje
) {
}