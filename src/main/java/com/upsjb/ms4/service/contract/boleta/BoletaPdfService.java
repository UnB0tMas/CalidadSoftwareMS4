// ruta: src/main/java/com/upsjb/ms4/service/contract/boleta/BoletaPdfService.java
package com.upsjb.ms4.service.contract.boleta;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.http.ResponseEntity;

public interface BoletaPdfService {

    byte[] generarPdf(Long idBoleta, AuthenticatedUserContext actor);

    byte[] generarPdfInterno(Long idBoleta);

    byte[] convertirHtmlAPdf(String html, String baseUri);

    String construirNombreArchivoPdf(Long idBoleta);

    ResponseEntity<byte[]> construirRespuestaPdfInline(Long idBoleta, AuthenticatedUserContext actor);
}