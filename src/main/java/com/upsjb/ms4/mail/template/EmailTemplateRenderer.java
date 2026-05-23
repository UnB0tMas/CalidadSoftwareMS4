// ruta: src/main/java/com/upsjb/ms4/mail/template/EmailTemplateRenderer.java
package com.upsjb.ms4.mail.template;

import com.upsjb.ms4.shared.exception.MailSendException;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Component
public class EmailTemplateRenderer {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("es-PE");

    private final TemplateEngine templateEngine;

    public EmailTemplateRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String render(String templatePath, Map<String, Object> model) {
        return render(templatePath, model, DEFAULT_LOCALE);
    }

    public String render(String templatePath, Map<String, Object> model, Locale locale) {
        String normalizedTemplate = normalizeTemplatePath(templatePath);

        try {
            Context context = new Context(locale == null ? DEFAULT_LOCALE : locale);
            context.setVariables(model == null ? Map.of() : model);
            return templateEngine.process(normalizedTemplate, context);
        } catch (Exception ex) {
            throw new MailSendException("No se pudo renderizar la plantilla de correo.", ex);
        }
    }

    private String normalizeTemplatePath(String templatePath) {
        if (templatePath == null || templatePath.isBlank()) {
            throw new MailSendException("La plantilla de correo es obligatoria.");
        }

        String normalized = templatePath.trim()
                .replace("\\", "/")
                .replaceAll("/{2,}", "/");

        normalized = stripPrefix(normalized, "classpath:/");
        normalized = stripPrefix(normalized, "/");
        normalized = stripPrefix(normalized, "src/main/resources/");
        normalized = stripPrefix(normalized, "templates/");
        normalized = stripSuffix(normalized, ".html");

        if (normalized.isBlank()) {
            throw new MailSendException("La plantilla de correo es obligatoria.");
        }

        return normalized;
    }

    private String stripPrefix(String value, String prefix) {
        return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }

    private String stripSuffix(String value, String suffix) {
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : value;
    }
}