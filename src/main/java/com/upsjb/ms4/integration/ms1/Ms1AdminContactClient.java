// ruta: src/main/java/com/upsjb/ms4/integration/ms1/Ms1AdminContactClient.java
package com.upsjb.ms4.integration.ms1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.config.InternalSecurityProperties;
import com.upsjb.ms4.shared.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Component
public class Ms1AdminContactClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final InternalSecurityProperties internalSecurityProperties;
    private final String adminContactsPath;

    public Ms1AdminContactClient(RestClient.Builder builder,
                                 ObjectMapper objectMapper,
                                 InternalSecurityProperties internalSecurityProperties,
                                 @Value("${ms4.integration.ms1.base-url:http://localhost:8081}") String baseUrl,
                                 @Value("${ms4.integration.ms1.admin-contacts-path:/api/internal/ms1/admins/contactos}") String adminContactsPath) {
        this.restClient = builder.baseUrl(normalizeBaseUrl(baseUrl)).build();
        this.objectMapper = objectMapper;
        this.internalSecurityProperties = internalSecurityProperties;
        this.adminContactsPath = normalizePath(adminContactsPath);
    }

    public List<AdminContact> findAdminContacts() {
        try {
            String rawResponse = restClient.get()
                    .uri(adminContactsPath)
                    .headers(this::addInternalHeaders)
                    .retrieve()
                    .body(String.class);

            return parseContacts(rawResponse);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("No se pudo consultar contactos ADMIN desde MS1.", ex);
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("No se pudo interpretar la respuesta de contactos ADMIN desde MS1.", ex);
        }
    }

    private List<AdminContact> parseContacts(String rawResponse) throws Exception {
        if (rawResponse == null || rawResponse.isBlank()) {
            return List.of();
        }

        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode data = unwrapDataNode(root);

        if (data == null || data.isNull() || !data.isArray()) {
            return List.of();
        }

        List<AdminContact> contacts = new ArrayList<>();

        for (JsonNode node : data) {
            AdminContact contact = new AdminContact(
                    longValue(node, "idUsuarioMs1", "id_usuario_ms1", "idUsuario", "id"),
                    textValue(node, "username", "usuario"),
                    textValue(node, "nombre", "nombreCompleto", "nombre_completo", "displayName"),
                    textValue(node, "email", "correo", "correoPrincipal", "correo_principal")
            );

            if (contact.valid()) {
                contacts.add(contact);
            }
        }

        return contacts.stream()
                .distinct()
                .toList();
    }

    private JsonNode unwrapDataNode(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }

        if (root.isArray()) {
            return root;
        }

        for (String field : List.of("data", "payload", "content", "items", "result")) {
            JsonNode candidate = root.get(field);

            if (candidate != null && candidate.isArray()) {
                return candidate;
            }
        }

        JsonNode nestedData = root.get("data");

        if (nestedData != null && nestedData.isObject()) {
            for (String field : List.of("content", "items", "result")) {
                JsonNode candidate = nestedData.get(field);

                if (candidate != null && candidate.isArray()) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private void addInternalHeaders(HttpHeaders headers) {
        if (!internalSecurityProperties.enabledSafe()) {
            return;
        }

        if (!internalSecurityProperties.configured()) {
            throw new ExternalServiceException("La clave interna para consultar MS1 no está configurada.");
        }

        headers.set(internalSecurityProperties.headerNameSafe(), internalSecurityProperties.serviceKeySafe());
    }

    private Long longValue(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);

            if (value != null && !value.isNull()) {
                if (value.isNumber()) {
                    return value.longValue();
                }

                if (value.isTextual() && !value.asText().isBlank()) {
                    return Long.valueOf(value.asText().trim());
                }
            }
        }

        return null;
    }

    private String textValue(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);

            if (value != null && !value.isNull() && !value.asText().isBlank()) {
                return value.asText().trim();
            }
        }

        return null;
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = baseUrl == null || baseUrl.isBlank()
                ? "http://localhost:8081"
                : baseUrl.trim();

        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    private String normalizePath(String path) {
        String value = path == null || path.isBlank()
                ? "/api/internal/ms1/admins/contactos"
                : path.trim();

        return value.startsWith("/") ? value : "/" + value;
    }

    public record AdminContact(
            Long idUsuarioMs1,
            String username,
            String nombre,
            String email
    ) {

        public AdminContact {
            username = normalize(username);
            nombre = normalize(nombre);
            email = normalize(email);
        }

        public boolean valid() {
            return email != null && !email.isBlank();
        }

        private static String normalize(String value) {
            return value == null || value.isBlank() ? null : value.trim();
        }
    }
}