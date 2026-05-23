// ruta: src/main/java/com/upsjb/ms4/shared/response/ErrorResponseFactory.java
package com.upsjb.ms4.shared.response;

import com.upsjb.ms4.dto.shared.ErrorResponseDto;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.metadata.RequestMetadataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ErrorResponseFactory {

    private final RequestMetadataExtractor metadataExtractor;
    private final Clock clock;

    public ErrorResponseFactory(RequestMetadataExtractor metadataExtractor, Clock clock) {
        this.metadataExtractor = metadataExtractor;
        this.clock = clock;
    }

    public ErrorResponseDto create(String code,
                                   String message,
                                   String technicalMessage,
                                   Map<String, List<String>> fieldErrors,
                                   HttpServletRequest request) {
        return new ErrorResponseDto(
                false,
                normalizeCode(code),
                message,
                technicalMessage,
                fieldErrors == null ? Map.of() : fieldErrors,
                LocalDateTime.now(clock),
                metadataExtractor.path(request),
                metadataExtractor.requestId(request),
                metadataExtractor.correlationId(request)
        );
    }

    public ErrorResponseDto create(String code,
                                   String message,
                                   HttpServletRequest request) {
        return create(code, message, null, Map.of(), request);
    }

    private String normalizeCode(String code) {
        return code == null || code.isBlank() ? ErrorCodes.INTERNAL_ERROR : code.trim();
    }
}