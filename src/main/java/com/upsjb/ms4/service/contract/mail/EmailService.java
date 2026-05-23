// ruta: src/main/java/com/upsjb/ms4/service/contract/mail/EmailService.java
package com.upsjb.ms4.service.contract.mail;

import com.upsjb.ms4.mail.model.EmailAttachment;
import com.upsjb.ms4.mail.model.EmailMessage;

import java.util.List;

public interface EmailService {

    void enviar(EmailMessage message);

    void enviarConAdjuntos(EmailMessage message, List<EmailAttachment> attachments);

    void validarDestinatario(String email);

    EmailMessage construirMensaje(String to, String nombre, String subject, String htmlBody);
}