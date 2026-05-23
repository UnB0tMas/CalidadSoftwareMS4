// ruta: src/main/java/com/upsjb/ms4/dto/shared/ApiResponseDto.java
package com.upsjb.ms4.dto.shared;

import java.time.LocalDateTime;

public record ApiResponseDto<T>(
        boolean success,
        String code,
        String message,
        T data,
        LocalDateTime timestamp,
        String path,
        String requestId,
        String correlationId
) {
}