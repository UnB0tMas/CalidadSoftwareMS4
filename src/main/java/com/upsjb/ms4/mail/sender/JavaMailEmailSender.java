// ruta: src/main/java/com/upsjb/ms4/mail/sender/JavaMailEmailSender.java
package com.upsjb.ms4.mail.sender;

import com.upsjb.ms4.config.CorreoOutboxProperties;
import com.upsjb.ms4.mail.model.EmailAttachment;
import com.upsjb.ms4.mail.model.EmailMessage;
import com.upsjb.ms4.shared.exception.MailSendException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JavaMailEmailSender {

    private final JavaMailSender javaMailSender;
    private final CorreoOutboxProperties properties;

    public JavaMailEmailSender(JavaMailSender javaMailSender,
                               CorreoOutboxProperties properties) {
        this.javaMailSender = javaMailSender;
        this.properties = properties;
    }

    public void send(EmailMessage message) {
        validateMessage(message);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    message.hasAttachments(),
                    StandardCharsets.UTF_8.name()
            );

            setFromIfConfigured(helper);
            helper.setTo(toArray(message.to()));
            helper.setSubject(message.subject());

            if (message.textBody() == null || message.textBody().isBlank()) {
                helper.setText(message.htmlBody(), true);
            } else {
                helper.setText(message.textBody(), message.htmlBody());
            }

            if (!message.ccSafe().isEmpty()) {
                helper.setCc(toArray(message.ccSafe()));
            }

            if (!message.bccSafe().isEmpty()) {
                helper.setBcc(toArray(message.bccSafe()));
            }

            for (var entry : message.headersSafe().entrySet()) {
                mimeMessage.addHeader(entry.getKey(), entry.getValue());
            }

            for (EmailAttachment attachment : message.attachmentsSafe()) {
                helper.addAttachment(
                        attachment.filename(),
                        new ByteArrayResource(attachment.content()),
                        attachment.contentType()
                );
            }

            javaMailSender.send(mimeMessage);
        } catch (MailSendException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MailSendException("No se pudo enviar el correo electrónico.", ex);
        }
    }

    private void validateMessage(EmailMessage message) {
        if (message == null) {
            throw new MailSendException("El mensaje de correo es obligatorio.");
        }

        if (message.to() == null || message.to().isEmpty()) {
            throw new MailSendException("Debe indicar al menos un destinatario.");
        }

        if (message.subject() == null || message.subject().isBlank()) {
            throw new MailSendException("El asunto del correo es obligatorio.");
        }

        if (message.htmlBody() == null || message.htmlBody().isBlank()) {
            throw new MailSendException("El cuerpo HTML del correo es obligatorio.");
        }
    }

    private void setFromIfConfigured(MimeMessageHelper helper) throws Exception {
        String defaultFrom = properties.defaultFrom();

        if (defaultFrom != null && !defaultFrom.isBlank()) {
            helper.setFrom(defaultFrom.trim());
        }
    }

    private String[] toArray(List<String> values) {
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toArray(String[]::new);
    }
}