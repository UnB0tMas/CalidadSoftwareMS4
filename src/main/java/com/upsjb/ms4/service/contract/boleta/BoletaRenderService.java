// ruta: src/main/java/com/upsjb/ms4/service/contract/boleta/BoletaRenderService.java
package com.upsjb.ms4.service.contract.boleta;

import com.upsjb.ms4.dto.boleta.response.BoletaPreviewResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.util.Map;

public interface BoletaRenderService {

    String renderizarHtml(Long idBoleta, AuthenticatedUserContext actor);

    String renderizarHtmlInterno(Long idBoleta);

    String renderizarHtmlDesdeModelo(Map<String, Object> model, String templatePath);

    BoletaPreviewResponseDto previsualizarComoDto(Long idBoleta, AuthenticatedUserContext actor);
}