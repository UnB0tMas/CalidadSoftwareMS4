// ruta: src/main/java/com/upsjb/ms4/integration/ms3/Ms3InternalStockSyncClient.java
package com.upsjb.ms4.integration.ms3;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.config.InternalSecurityProperties;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.shared.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class Ms3InternalStockSyncClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final InternalSecurityProperties internalSecurityProperties;
    private final String pendingEventsPath;
    private final String reconcilePath;

    public Ms3InternalStockSyncClient(RestClient.Builder builder,
                                      ObjectMapper objectMapper,
                                      InternalSecurityProperties internalSecurityProperties,
                                      @Value("${ms4.integration.ms3.base-url:http://localhost:8083}") String baseUrl,
                                      @Value("${ms4.integration.ms3.pending-events-path:/api/internal/ms3/stock-sync/ms4-events}") String pendingEventsPath,
                                      @Value("${ms4.integration.ms3.reconcile-path:/api/internal/ms3/stock-sync/reconcile/{idempotencyKey}}") String reconcilePath) {
        this.restClient = builder.baseUrl(normalizeBaseUrl(baseUrl)).build();
        this.objectMapper = objectMapper;
        this.internalSecurityProperties = internalSecurityProperties;
        this.pendingEventsPath = normalizePath(pendingEventsPath);
        this.reconcilePath = normalizePath(reconcilePath);
    }

    public boolean healthCheck() {
        try {
            restClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (RestClientException ex) {
            return false;
        }
    }

    public PageResponseDto<InventarioEventoPendienteResponseDto> listarPendientesReconocidosPorMs3(PageRequestDto page) {
        try {
            String rawResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(pendingEventsPath)
                            .queryParam("page", pageNumber(page))
                            .queryParam("size", pageSize(page))
                            .queryParamIfPresent("sortBy", optional(page == null ? null : page.sortBy()))
                            .queryParamIfPresent("sortDirection", optional(page == null ? null : page.sortDirection()))
                            .build())
                    .headers(this::addInternalHeaders)
                    .retrieve()
                    .body(String.class);

            return parsePageResponse(rawResponse);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("No se pudo consultar eventos de stock en MS3.", ex);
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("No se pudo interpretar la respuesta de eventos de stock en MS3.", ex);
        }
    }

    public void solicitarReconciliacion(String idempotencyKey) {
        String key = normalizeRequired(idempotencyKey, "La idempotencyKey es obligatoria para reconciliar con MS3.");

        try {
            restClient.post()
                    .uri(reconcilePath, key)
                    .headers(this::addInternalHeaders)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new ExternalServiceException("No se pudo solicitar reconciliación de stock a MS3.", ex);
        }
    }

    private PageResponseDto<InventarioEventoPendienteResponseDto> parsePageResponse(String rawResponse) throws Exception {
        if (rawResponse == null || rawResponse.isBlank()) {
            return emptyPageResponse();
        }

        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode pageNode = unwrapDataNode(root);

        JavaType type = objectMapper.getTypeFactory().constructParametricType(
                PageResponseDto.class,
                InventarioEventoPendienteResponseDto.class
        );

        return objectMapper.readerFor(type).readValue(pageNode);
    }

    private JsonNode unwrapDataNode(JsonNode root) {
        if (root == null || root.isNull()) {
            throw new ExternalServiceException("MS3 devolvió una respuesta vacía.");
        }

        if (root.has("content") && root.get("content").isArray()) {
            return root;
        }

        for (String field : java.util.List.of("data", "payload", "result")) {
            JsonNode candidate = root.get(field);

            if (candidate != null && !candidate.isNull()) {
                return candidate;
            }
        }

        return root;
    }

    private PageResponseDto<InventarioEventoPendienteResponseDto> emptyPageResponse() {
        return new PageResponseDto<>(
                java.util.List.of(),
                0,
                20,
                0,
                0,
                true,
                true,
                true,
                null,
                null
        );
    }

    private void addInternalHeaders(HttpHeaders headers) {
        if (!internalSecurityProperties.enabledSafe()) {
            return;
        }

        if (!internalSecurityProperties.configured()) {
            throw new ExternalServiceException("La clave interna para consultar MS3 no está configurada.");
        }

        headers.set(internalSecurityProperties.headerNameSafe(), internalSecurityProperties.serviceKeySafe());
    }

    private int pageNumber(PageRequestDto page) {
        return page == null || page.page() == null || page.page() < 0 ? 0 : page.page();
    }

    private int pageSize(PageRequestDto page) {
        if (page == null || page.size() == null) {
            return 20;
        }

        if (page.size() < 1) {
            return 20;
        }

        return Math.min(page.size(), 100);
    }

    private java.util.Optional<String> optional(String value) {
        return value == null || value.isBlank()
                ? java.util.Optional.empty()
                : java.util.Optional.of(value.trim());
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ExternalServiceException(message);
        }

        return value.trim();
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = baseUrl == null || baseUrl.isBlank()
                ? "http://localhost:8083"
                : baseUrl.trim();

        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new ExternalServiceException("La ruta interna de MS3 es obligatoria.");
        }

        String normalized = path.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }
}