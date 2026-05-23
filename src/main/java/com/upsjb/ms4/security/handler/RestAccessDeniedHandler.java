// ruta: src/main/java/com/upsjb/ms4/security/handler/RestAccessDeniedHandler.java
package com.upsjb.ms4.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.dto.shared.ErrorResponseDto;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.response.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseFactory errorResponseFactory;
    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ErrorResponseFactory errorResponseFactory,
                                   ObjectMapper objectMapper) {
        this.errorResponseFactory = errorResponseFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        ErrorResponseDto body = errorResponseFactory.create(
                ErrorCodes.FORBIDDEN,
                "No tiene permisos para realizar esta acción.",
                null,
                null,
                request
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        objectMapper.writeValue(response.getWriter(), body);
    }
}