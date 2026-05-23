// ruta: src/main/java/com/upsjb/ms4/mail/model/EmailMessage.java
package com.upsjb.ms4.mail.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EmailMessage(

        @NotEmpty(message = "Debe indicar al menos un destinatario.")
        List<@Email(message = "Debe indicar correos válidos.") String> to,

        List<@Email(message = "Debe indicar correos válidos en copia.") String> cc,

        List<@Email(message = "Debe indicar correos válidos en copia oculta.") String> bcc,

        @NotBlank(message = "El asunto es obligatorio.")
        String subject,

        @NotBlank(message = "El cuerpo HTML del correo es obligatorio.")
        String htmlBody,

        String textBody,

        Map<String, String> headers,

        List<@Valid EmailAttachment> attachments
) {

    public EmailMessage {
        to = normalizeRecipients(to);
        cc = normalizeRecipients(cc);
        bcc = normalizeRecipients(bcc);
        subject = normalize(subject);
        htmlBody = normalize(htmlBody);
        textBody = normalize(textBody);
        headers = normalizeHeaders(headers);
        attachments = attachments == null
                ? List.of()
                : attachments.stream()
                .filter(attachment -> attachment != null && attachment.hasContent())
                .toList();
    }

    public List<String> ccSafe() {
        return cc == null ? List.of() : cc;
    }

    public List<String> bccSafe() {
        return bcc == null ? List.of() : bcc;
    }

    public Map<String, String> headersSafe() {
        return headers == null ? Map.of() : headers;
    }

    public List<EmailAttachment> attachmentsSafe() {
        return attachments == null ? List.of() : attachments;
    }

    public boolean hasAttachments() {
        return !attachmentsSafe().isEmpty();
    }

    private static List<String> normalizeRecipients(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private static Map<String, String> normalizeHeaders(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, String> normalized = new LinkedHashMap<>();

        values.forEach((key, value) -> {
            String headerName = normalizeHeaderName(key);
            String headerValue = normalizeHeaderValue(value);

            if (headerName != null && headerValue != null) {
                normalized.put(headerName, headerValue);
            }
        });

        return normalized.isEmpty() ? Map.of() : Map.copyOf(normalized);
    }

    private static String normalizeHeaderName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.contains("\r") || normalized.contains("\n") || normalized.contains(":")) {
            return null;
        }

        return normalized;
    }

    private static String normalizeHeaderValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.contains("\r") || normalized.contains("\n")) {
            return null;
        }

        return normalized;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}