// ruta: src/main/java/com/upsjb/ms4/shared/metadata/RequestMetadataExtractor.java
package com.upsjb.ms4.shared.metadata;

import com.upsjb.ms4.shared.constants.HeaderNames;
import com.upsjb.ms4.shared.constants.Ms4Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequestMetadataExtractor {

    private static final String REQUEST_ID_ATTRIBUTE =
            RequestMetadataExtractor.class.getName() + ".REQUEST_ID";

    private static final String CORRELATION_ID_ATTRIBUTE =
            RequestMetadataExtractor.class.getName() + ".CORRELATION_ID";

    public String requestId(HttpServletRequest request) {
        if (request == null) {
            return UUID.randomUUID().toString();
        }

        String cached = attribute(request, REQUEST_ID_ATTRIBUTE);
        if (cached != null) {
            return cached;
        }

        String value = firstNonBlank(
                header(request, HeaderNames.REQUEST_ID, Ms4Constants.MAX_REQUEST_ID_LENGTH),
                header(request, HeaderNames.REQUEST_ID_ALT, Ms4Constants.MAX_REQUEST_ID_LENGTH),
                UUID.randomUUID().toString()
        );

        request.setAttribute(REQUEST_ID_ATTRIBUTE, value);
        return value;
    }

    public String correlationId(HttpServletRequest request) {
        if (request == null) {
            return UUID.randomUUID().toString();
        }

        String cached = attribute(request, CORRELATION_ID_ATTRIBUTE);
        if (cached != null) {
            return cached;
        }

        String value = firstNonBlank(
                header(request, HeaderNames.CORRELATION_ID, Ms4Constants.MAX_CORRELATION_ID_LENGTH),
                header(request, HeaderNames.CORRELATION_ID_ALT, Ms4Constants.MAX_CORRELATION_ID_LENGTH),
                requestId(request)
        );

        request.setAttribute(CORRELATION_ID_ATTRIBUTE, value);
        return value;
    }

    public String ip(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String forwardedFor = header(request, HeaderNames.FORWARDED_FOR, Ms4Constants.MAX_HEADER_VALUE_LENGTH);
        if (forwardedFor != null) {
            String firstIp = forwardedFor.split(",")[0].trim();
            return sanitize(firstIp, Ms4Constants.MAX_IP_LENGTH);
        }

        String realIp = header(request, HeaderNames.REAL_IP, Ms4Constants.MAX_IP_LENGTH);
        if (realIp != null) {
            return realIp;
        }

        return sanitize(request.getRemoteAddr(), Ms4Constants.MAX_IP_LENGTH);
    }

    public String userAgent(HttpServletRequest request) {
        return header(request, HeaderNames.USER_AGENT, Ms4Constants.MAX_USER_AGENT_LENGTH);
    }

    public String path(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String prefix = header(request, HeaderNames.FORWARDED_PREFIX, Ms4Constants.MAX_PATH_LENGTH);
        String uri = request.getRequestURI();

        if (prefix == null || prefix.isBlank() || uri == null || uri.startsWith(prefix)) {
            return sanitize(uri, Ms4Constants.MAX_PATH_LENGTH);
        }

        return sanitize(prefix + uri, Ms4Constants.MAX_PATH_LENGTH);
    }

    public String method(HttpServletRequest request) {
        return request == null ? null : request.getMethod();
    }

    public String header(HttpServletRequest request, String name) {
        return header(request, name, Ms4Constants.MAX_HEADER_VALUE_LENGTH);
    }

    public String header(HttpServletRequest request, String name, int maxLength) {
        if (request == null || name == null || name.isBlank()) {
            return null;
        }

        return sanitize(request.getHeader(name), maxLength);
    }

    private String attribute(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value instanceof String text && !text.isBlank() ? text : null;
    }

    private String sanitize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = value.trim()
                .replace("\r", "")
                .replace("\n", "");

        if (cleaned.isBlank()) {
            return null;
        }

        int safeMax = maxLength <= 0 ? Ms4Constants.MAX_HEADER_VALUE_LENGTH : maxLength;
        return cleaned.length() <= safeMax ? cleaned : cleaned.substring(0, safeMax);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }
}