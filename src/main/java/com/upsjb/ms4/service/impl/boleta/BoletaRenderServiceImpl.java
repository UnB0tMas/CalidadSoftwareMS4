// ruta: src/main/java/com/upsjb/ms4/service/impl/boleta/BoletaRenderServiceImpl.java
package com.upsjb.ms4.service.impl.boleta;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.dto.boleta.response.BoletaPreviewResponseDto;
import com.upsjb.ms4.mapper.boleta.BoletaMapper;
import com.upsjb.ms4.policy.BoletaPolicy;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.boleta.BoletaRenderService;
import com.upsjb.ms4.service.contract.boleta.BoletaService;
import com.upsjb.ms4.service.contract.boleta.BoletaTemplateModelFactory;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.validator.BoletaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Service
public class BoletaRenderServiceImpl implements BoletaRenderService {

    private static final Logger log = LoggerFactory.getLogger(BoletaRenderServiceImpl.class);
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("es-PE");

    private final BoletaService boletaService;
    private final BoletaTemplateModelFactory templateModelFactory;
    private final BoletaMapper boletaMapper;
    private final BoletaPolicy boletaPolicy;
    private final BoletaValidator boletaValidator;
    private final TemplateEngine templateEngine;

    public BoletaRenderServiceImpl(BoletaService boletaService,
                                   BoletaTemplateModelFactory templateModelFactory,
                                   BoletaMapper boletaMapper,
                                   BoletaPolicy boletaPolicy,
                                   BoletaValidator boletaValidator,
                                   TemplateEngine templateEngine) {
        this.boletaService = boletaService;
        this.templateModelFactory = templateModelFactory;
        this.boletaMapper = boletaMapper;
        this.boletaPolicy = boletaPolicy;
        this.boletaValidator = boletaValidator;
        this.templateEngine = templateEngine;
    }

    @Override
    @Transactional(readOnly = true)
    public String renderizarHtml(Long idBoleta, AuthenticatedUserContext actor) {
        Boleta boleta = boletaService.resolverBoletaParaRender(idBoleta);
        boletaPolicy.authorizeVerBoleta(actor, boleta);

        Map<String, Object> model = templateModelFactory.construirModeloBoleta(idBoleta);
        String templatePath = templateModelFactory.resolverRutaTemplateBoleta(boleta);

        return renderizarHtmlDesdeModelo(model, templatePath);
    }

    @Override
    @Transactional(readOnly = true)
    public String renderizarHtmlInterno(Long idBoleta) {
        Boleta boleta = boletaService.resolverBoletaParaRender(idBoleta);

        Map<String, Object> model = templateModelFactory.construirModeloBoleta(idBoleta);
        String templatePath = templateModelFactory.resolverRutaTemplateBoleta(boleta);

        return renderizarHtmlDesdeModelo(model, templatePath);
    }

    @Override
    public String renderizarHtmlDesdeModelo(Map<String, Object> model, String templatePath) {
        boletaValidator.validarTemplatePath(templatePath);

        String normalizedTemplate = normalizeTemplatePath(templatePath);

        try {
            Context context = new Context(DEFAULT_LOCALE);
            context.setVariables(model == null ? Map.of() : model);
            return templateEngine.process(normalizedTemplate, context);
        } catch (RuntimeException ex) {
            log.error("Error renderizando HTML de boleta. templatePath={}", templatePath, ex);
            throw new BusinessException(
                    ErrorCodes.INTERNAL_ERROR,
                    "No se pudo renderizar la boleta.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ex
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaPreviewResponseDto previsualizarComoDto(Long idBoleta, AuthenticatedUserContext actor) {
        Boleta boleta = boletaService.resolverBoletaParaRender(idBoleta);
        boletaPolicy.authorizeVerBoleta(actor, boleta);

        String html = renderizarHtml(idBoleta, actor);
        return boletaMapper.toPreviewResponse(boleta, html);
    }

    private String normalizeTemplatePath(String templatePath) {
        String normalized = templatePath.trim()
                .replace("\\", "/")
                .replaceAll("/{2,}", "/");

        normalized = stripPrefix(normalized, "classpath:/");
        normalized = stripPrefix(normalized, "/");
        normalized = stripPrefix(normalized, "src/main/resources/");
        normalized = stripPrefix(normalized, "templates/");
        normalized = stripSuffix(normalized, ".html");

        boletaValidator.validarTemplatePath(normalized);
        return normalized;
    }

    private String stripPrefix(String value, String prefix) {
        return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }

    private String stripSuffix(String value, String suffix) {
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : value;
    }
}