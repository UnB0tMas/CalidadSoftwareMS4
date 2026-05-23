// ruta: src/main/java/com/upsjb/ms4/integration/ms1/AdminRecipientProvider.java
package com.upsjb.ms4.integration.ms1;

import com.upsjb.ms4.config.CorreoOutboxProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class AdminRecipientProvider {

    private static final Logger log = LoggerFactory.getLogger(AdminRecipientProvider.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final Ms1AdminContactClient ms1AdminContactClient;
    private final CorreoOutboxProperties correoOutboxProperties;

    public AdminRecipientProvider(Ms1AdminContactClient ms1AdminContactClient,
                                  CorreoOutboxProperties correoOutboxProperties) {
        this.ms1AdminContactClient = ms1AdminContactClient;
        this.correoOutboxProperties = correoOutboxProperties;
    }

    public List<String> adminEmails() {
        List<String> fromMs1 = findFromMs1();

        if (!fromMs1.isEmpty()) {
            return fromMs1;
        }

        return fallbackEmails();
    }

    private List<String> findFromMs1() {
        try {
            return ms1AdminContactClient.findAdminContacts().stream()
                    .map(Ms1AdminContactClient.AdminContact::email)
                    .map(this::normalizeEmail)
                    .filter(this::validEmail)
                    .distinct()
                    .toList();
        } catch (Exception ex) {
            log.warn(
                    "No se pudo obtener destinatarios ADMIN desde MS1. Se usará fallback si existe. Motivo: {}",
                    ex.getMessage()
            );
            return List.of();
        }
    }

    private List<String> fallbackEmails() {
        String fallback = correoOutboxProperties.adminAlertEmail();

        if (fallback == null || fallback.isBlank()) {
            return List.of();
        }

        return Arrays.stream(fallback.split("[,;]"))
                .map(this::normalizeEmail)
                .filter(this::validEmail)
                .distinct()
                .toList();
    }

    private String normalizeEmail(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean validEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value).matches();
    }
}