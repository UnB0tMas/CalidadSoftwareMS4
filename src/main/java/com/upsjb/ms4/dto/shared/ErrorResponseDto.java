// ruta: src/main/java/com/upsjb/ms4/dto/shared/ErrorResponseDto.java
package com.upsjb.ms4.dto.shared;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponseDto(
        boolean success,
        String code,
        String message,
        String technicalMessage,
        Map<String, List<String>> fieldErrors,
        LocalDateTime timestamp,
        String path,
        String requestId,
        String correlationId
) {
}