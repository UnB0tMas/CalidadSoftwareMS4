// ruta: src/main/java/com/upsjb/ms4/service/impl/mail/EmailServiceImpl.java
package com.upsjb.ms4.service.impl.mail;

import com.upsjb.ms4.mail.model.EmailAttachment;
import com.upsjb.ms4.mail.model.EmailMessage;
import com.upsjb.ms4.mail.sender.JavaMailEmailSender;
import com.upsjb.ms4.service.contract.mail.EmailService;
import com.upsjb.ms4.shared.exception.MailSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final JavaMailEmailSender javaMailEmailSender;

    public EmailServiceImpl(JavaMailEmailSender javaMailEmailSender) {
        this.javaMailEmailSender = javaMailEmailSender;
    }

    @Override
    public void enviar(EmailMessage message) {
        validarMensaje(message);
        enviarSeguro(message);
    }

    @Override
    public void enviarConAdjuntos(EmailMessage message, List<EmailAttachment> attachments) {
        validarMensaje(message);
        validarAdjuntos(attachments);

        EmailMessage messageWithAttachments = new EmailMessage(
                message.to(),
                message.ccSafe(),
                message.bccSafe(),
                message.subject(),
                message.htmlBody(),
                message.textBody(),
                message.headersSafe(),
                attachments == null || attachments.isEmpty() ? message.attachmentsSafe() : attachments
        );

        enviarSeguro(messageWithAttachments);
    }

    @Override
    public void validarDestinatario(String email) {
        if (email == null || email.isBlank()) {
            throw new MailSendException("El destinatario del correo es obligatorio.");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new MailSendException("El destinatario del correo no tiene formato válido.");
        }
    }

    @Override
    public EmailMessage construirMensaje(String to, String nombre, String subject, String htmlBody) {
        validarDestinatario(to);

        return new EmailMessage(
                List.of(to.trim()),
                List.of(),
                List.of(),
                subject,
                htmlBody,
                null,
                nombre == null || nombre.isBlank() ? Map.of() : Map.of("X-MS4-Destinatario-Nombre", nombre.trim()),
                List.of()
        );
    }

    private void enviarSeguro(EmailMessage message) {
        try {
            javaMailEmailSender.send(message);
        } catch (MailSendException ex) {
            log.error(
                    "Error enviando correo. destinatarios={}, asunto={}, adjuntos={}",
                    message.to(),
                    message.subject(),
                    message.attachmentsSafe().size(),
                    ex
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico inesperado enviando correo. destinatarios={}, asunto={}, adjuntos={}",
                    message.to(),
                    message.subject(),
                    message.attachmentsSafe().size(),
                    ex
            );
            throw new MailSendException("No se pudo enviar el correo electrónico.", ex);
        }
    }

    private void validarMensaje(EmailMessage message) {
        if (message == null) {
            throw new MailSendException("El mensaje de correo es obligatorio.");
        }

        if (message.to() == null || message.to().isEmpty()) {
            throw new MailSendException("Debe indicar al menos un destinatario.");
        }

        message.to().forEach(this::validarDestinatario);
        message.ccSafe().forEach(this::validarDestinatario);
        message.bccSafe().forEach(this::validarDestinatario);

        if (message.subject() == null || message.subject().isBlank()) {
            throw new MailSendException("El asunto del correo es obligatorio.");
        }

        if (message.htmlBody() == null || message.htmlBody().isBlank()) {
            throw new MailSendException("El cuerpo HTML del correo es obligatorio.");
        }

        validarAdjuntos(message.attachmentsSafe());
    }

    private void validarAdjuntos(List<EmailAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        attachments.forEach(attachment -> {
            if (attachment == null || !attachment.hasContent()) {
                throw new MailSendException("El adjunto del correo no tiene contenido válido.");
            }

            if (attachment.filename() == null || attachment.filename().isBlank()) {
                throw new MailSendException("El nombre del adjunto es obligatorio.");
            }

            if (attachment.contentType() == null || attachment.contentType().isBlank()) {
                throw new MailSendException("El content type del adjunto es obligatorio.");
            }
        });
    }
}