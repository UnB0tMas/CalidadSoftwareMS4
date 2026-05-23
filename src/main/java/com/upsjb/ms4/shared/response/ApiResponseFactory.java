// ruta: src/main/java/com/upsjb/ms4/shared/response/ApiResponseFactory.java
package com.upsjb.ms4.shared.response;

import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.metadata.RequestMetadataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class ApiResponseFactory {

    private final RequestMetadataExtractor metadataExtractor;
    private final Clock clock;

    public ApiResponseFactory(RequestMetadataExtractor metadataExtractor, Clock clock) {
        this.metadataExtractor = metadataExtractor;
        this.clock = clock;
    }

    public <T> ApiResponseDto<T> ok(T data, String message, HttpServletRequest request) {
        return create(true, ErrorCodes.OK, message, data, request);
    }

    public <T> ApiResponseDto<T> created(T data, String message, HttpServletRequest request) {
        return create(true, ErrorCodes.CREATED, message, data, request);
    }

    public <T> ApiResponseDto<T> accepted(T data, String message, HttpServletRequest request) {
        return create(true, ErrorCodes.ACCEPTED, message, data, request);
    }

    public <T> ApiResponseDto<T> create(boolean success,
                                        String code,
                                        String message,
                                        T data,
                                        HttpServletRequest request) {
        return new ApiResponseDto<>(
                success,
                normalizeCode(code),
                message,
                data,
                LocalDateTime.now(clock),
                metadataExtractor.path(request),
                metadataExtractor.requestId(request),
                metadataExtractor.correlationId(request)
        );
    }

    private String normalizeCode(String code) {
        return code == null || code.isBlank() ? ErrorCodes.OK : code.trim();
    }
}