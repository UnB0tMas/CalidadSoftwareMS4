// ruta: src/main/java/com/upsjb/ms4/service/impl/boleta/BoletaMailServiceImpl.java
package com.upsjb.ms4.service.impl.boleta;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.entity.mail.CorreoOutbox;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.boleta.request.BoletaReenvioCorreoRequestDto;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import com.upsjb.ms4.mail.model.EmailAttachment;
import com.upsjb.ms4.mail.model.EmailMessage;
import com.upsjb.ms4.mail.template.EmailTemplateRenderer;
import com.upsjb.ms4.policy.BoletaPolicy;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.boleta.BoletaMailService;
import com.upsjb.ms4.service.contract.boleta.BoletaPdfService;
import com.upsjb.ms4.service.contract.boleta.BoletaService;
import com.upsjb.ms4.service.contract.boleta.BoletaTemplateModelFactory;
import com.upsjb.ms4.service.contract.mail.CorreoOutboxService;
import com.upsjb.ms4.service.contract.mail.EmailService;
import com.upsjb.ms4.shared.constants.HeaderNames;
import com.upsjb.ms4.validator.BoletaValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BoletaMailServiceImpl implements BoletaMailService {

    private static final String ENTIDAD_ORIGEN_BOLETA = "BOLETA";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final BoletaService boletaService;
    private final BoletaPdfService boletaPdfService;
    private final BoletaTemplateModelFactory templateModelFactory;
    private final CorreoOutboxService correoOutboxService;
    private final EmailService emailService;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final BoletaPolicy boletaPolicy;
    private final BoletaValidator boletaValidator;

    public BoletaMailServiceImpl(BoletaService boletaService,
                                 BoletaPdfService boletaPdfService,
                                 BoletaTemplateModelFactory templateModelFactory,
                                 CorreoOutboxService correoOutboxService,
                                 EmailService emailService,
                                 EmailTemplateRenderer emailTemplateRenderer,
                                 BoletaPolicy boletaPolicy,
                                 BoletaValidator boletaValidator) {
        this.boletaService = boletaService;
        this.boletaPdfService = boletaPdfService;
        this.templateModelFactory = templateModelFactory;
        this.correoOutboxService = correoOutboxService;
        this.emailService = emailService;
        this.emailTemplateRenderer = emailTemplateRenderer;
        this.boletaPolicy = boletaPolicy;
        this.boletaValidator = boletaValidator;
    }

    @Override
    @Transactional
    public CorreoOutboxResponseDto programarEnvioBoletaCompraFisica(Long idBoleta, AuthenticatedUserContext actor) {
        return boletaService.programarCorreoBoleta(idBoleta, TipoCorreo.BOLETA_COMPRA_FISICA, actor);
    }

    @Override
    @Transactional
    public CorreoOutboxResponseDto programarEnvioBoletaCompraOnline(Long idBoleta, AuthenticatedUserContext actor) {
        return boletaService.programarCorreoBoleta(idBoleta, TipoCorreo.BOLETA_COMPRA_ONLINE, actor);
    }

    @Override
    @Transactional
    public CorreoOutboxResponseDto programarReenvioBoleta(Long idBoleta,
                                                          BoletaReenvioCorreoRequestDto request,
                                                          AuthenticatedUserContext actor) {
        Boleta boleta = boletaService.resolverBoletaParaRender(idBoleta);
        boletaPolicy.authorizeReenviarCorreo(actor, boleta);

        String correoDestino = firstNonBlank(
                request == null ? null : request.correoDestino(),
                boleta.getCorreoCliente()
        );
        String nombreDestino = firstNonBlank(
                request == null ? null : request.nombreDestino(),
                boleta.getNombreCliente()
        );

        boletaValidator.validarCorreoCliente(correoDestino);
        boletaValidator.validarObservacionReenvio(request == null ? null : request.observacion());

        TipoCorreo tipoCorreo = resolverTipoCorreoPorCanal(boleta);

        return correoOutboxService.programarCorreo(
                tipoCorreo,
                ENTIDAD_ORIGEN_BOLETA,
                boleta.getId(),
                boleta.getId(),
                correoDestino,
                nombreDestino,
                construirAsunto(boleta),
                actor
        );
    }

    @Override
    @Transactional
    public void enviarBoletaDesdeOutbox(CorreoOutbox correoOutbox) {
        boletaValidator.validarCorreoOutboxBoleta(correoOutbox);

        EmailMessage message = construirEmailBoleta(correoOutbox);
        emailService.enviar(message);

        boletaService.marcarBoletaEnviadaPorCorreo(correoOutbox.getIdBoleta());
    }

    @Override
    @Transactional(readOnly = true)
    public EmailMessage construirEmailBoleta(CorreoOutbox correoOutbox) {
        boletaValidator.validarCorreoOutboxBoleta(correoOutbox);

        Boleta boleta = boletaService.resolverBoletaParaRender(correoOutbox.getIdBoleta());
        Map<String, Object> model = templateModelFactory.construirModeloCorreoBoleta(boleta.getId());
        String templatePath = templateModelFactory.resolverRutaTemplateCorreo(boleta);
        String htmlBody = emailTemplateRenderer.render(templatePath, model);

        byte[] pdf = boletaPdfService.generarPdfInterno(boleta.getId());
        String filename = boletaPdfService.construirNombreArchivoPdf(boleta.getId());

        EmailAttachment attachment = new EmailAttachment(filename, PDF_CONTENT_TYPE, pdf);

        return new EmailMessage(
                List.of(correoOutbox.getDestinatarioEmail()),
                List.of(),
                List.of(),
                correoOutbox.getAsunto(),
                htmlBody,
                null,
                Map.of(
                        HeaderNames.CORRELATION_ID,
                        correoOutbox.getCorrelationId() == null ? "" : correoOutbox.getCorrelationId(),
                        HeaderNames.REQUEST_ID,
                        correoOutbox.getRequestId() == null ? "" : correoOutbox.getRequestId()
                ),
                List.of(attachment)
        );
    }

    private TipoCorreo resolverTipoCorreoPorCanal(Boleta boleta) {
        if (boleta.getVenta() != null
                && boleta.getVenta().getCanalVenta() != null
                && boleta.getVenta().getCanalVenta().isOnline()) {
            return TipoCorreo.BOLETA_COMPRA_ONLINE;
        }

        return TipoCorreo.BOLETA_COMPRA_FISICA;
    }

    private String construirAsunto(Boleta boleta) {
        return "Boleta " + boleta.getCodigoBoleta();
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }

        return fallback == null ? null : fallback.trim();
    }
}