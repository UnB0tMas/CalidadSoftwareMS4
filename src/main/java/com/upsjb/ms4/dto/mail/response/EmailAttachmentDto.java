// ruta: src/main/java/com/upsjb/ms4/dto/mail/response/EmailAttachmentDto.java
package com.upsjb.ms4.dto.mail.response;

public record EmailAttachmentDto(
        String filename,
        String contentType,
        byte[] content
) {
}