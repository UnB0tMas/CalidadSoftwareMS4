// ruta: src/main/java/com/upsjb/ms4/mail/model/EmailAttachment.java
package com.upsjb.ms4.mail.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;

public record EmailAttachment(

        @NotBlank(message = "El nombre del adjunto es obligatorio.")
        String filename,

        @NotBlank(message = "El content type del adjunto es obligatorio.")
        String contentType,

        @NotNull(message = "El contenido del adjunto es obligatorio.")
        byte[] content
) {

        public EmailAttachment {
                filename = normalize(filename);
                contentType = normalize(contentType);
                content = content == null ? null : Arrays.copyOf(content, content.length);
        }

        @Override
        public byte[] content() {
                return content == null ? null : Arrays.copyOf(content, content.length);
        }

        public boolean hasContent() {
                return content != null && content.length > 0;
        }

        private static String normalize(String value) {
                return value == null ? null : value.trim();
        }
}