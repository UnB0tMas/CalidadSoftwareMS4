// ruta: src/main/java/com/upsjb/ms4/security/filter/InternalServiceKeyFilter.java
package com.upsjb.ms4.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.config.InternalSecurityProperties;
import com.upsjb.ms4.dto.shared.ErrorResponseDto;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.constants.HeaderNames;
import com.upsjb.ms4.shared.response.ErrorResponseFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalServiceKeyFilter extends OncePerRequestFilter {

    public static final String INTERNAL_SERVICE_AUTHENTICATED_ATTRIBUTE =
            InternalServiceKeyFilter.class.getName() + ".AUTHENTICATED";

    private final InternalSecurityProperties properties;
    private final ErrorResponseFactory errorResponseFactory;
    private final ObjectMapper objectMapper;

    public InternalServiceKeyFilter(InternalSecurityProperties properties,
                                    ErrorResponseFactory errorResponseFactory,
                                    ObjectMapper objectMapper) {
        this.properties = properties;
        this.errorResponseFactory = errorResponseFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = resolvePath(request);
        return !isInternalMs4Path(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.enabledSafe()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!properties.configured()) {
            writeUnauthorized(response, request, "La clave interna del servicio no está configurada.");
            return;
        }

        String receivedKey = request.getHeader(properties.headerNameSafe());

        if (receivedKey == null && !HeaderNames.INTERNAL_SERVICE_KEY.equals(properties.headerNameSafe())) {
            receivedKey = request.getHeader(HeaderNames.INTERNAL_SERVICE_KEY);
        }

        if (!matchesServiceKey(receivedKey)) {
            writeUnauthorized(response, request, "Clave interna inválida.");
            return;
        }

        request.setAttribute(INTERNAL_SERVICE_AUTHENTICATED_ATTRIBUTE, Boolean.TRUE);
        filterChain.doFilter(request, response);
    }

    private boolean matchesServiceKey(String receivedKey) {
        if (receivedKey == null || receivedKey.isBlank()) {
            return false;
        }

        byte[] expected = properties.serviceKeyBytes();
        byte[] received = receivedKey.trim().getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(expected, received);
    }

    private boolean isInternalMs4Path(String path) {
        return ApiPaths.INTERNAL.equals(path)
                || path.startsWith(ApiPaths.INTERNAL + "/");
    }

    private String resolvePath(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isBlank()) {
            return servletPath;
        }

        String uri = request.getRequestURI();
        return uri == null ? "" : uri;
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   HttpServletRequest request,
                                   String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        ErrorResponseDto body = errorResponseFactory.create(
                ErrorCodes.UNAUTHORIZED,
                message,
                null,
                null,
                request
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        objectMapper.writeValue(response.getWriter(), body);
    }
}