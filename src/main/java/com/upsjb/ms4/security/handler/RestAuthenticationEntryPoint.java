// ruta: src/main/java/com/upsjb/ms4/security/handler/RestAuthenticationEntryPoint.java
package com.upsjb.ms4.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.dto.shared.ErrorResponseDto;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.response.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseFactory errorResponseFactory;
    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ErrorResponseFactory errorResponseFactory,
                                        ObjectMapper objectMapper) {
        this.errorResponseFactory = errorResponseFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        ErrorResponseDto body = errorResponseFactory.create(
                ErrorCodes.UNAUTHORIZED,
                "Debe autenticarse con un token JWT válido para acceder al recurso.",
                null,
                null,
                request
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        objectMapper.writeValue(response.getWriter(), body);
    }
}