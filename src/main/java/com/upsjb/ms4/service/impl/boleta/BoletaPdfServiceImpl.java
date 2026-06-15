package com.upsjb.ms4.service.impl.boleta;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.policy.BoletaPolicy;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.boleta.BoletaPdfService;
import com.upsjb.ms4.service.contract.boleta.BoletaRenderService;
import com.upsjb.ms4.service.contract.boleta.BoletaService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.validator.BoletaValidator;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoletaPdfServiceImpl
        implements BoletaPdfService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BoletaPdfServiceImpl.class
            );

    private static final String DEFAULT_BASE_URI =
            "classpath:/templates/";

    private static final String PDF_CONTENT_TYPE =
            "application/pdf";

    private final BoletaService boletaService;
    private final BoletaRenderService boletaRenderService;
    private final BoletaPolicy boletaPolicy;
    private final BoletaValidator boletaValidator;

    public BoletaPdfServiceImpl(
            BoletaService boletaService,
            BoletaRenderService boletaRenderService,
            BoletaPolicy boletaPolicy,
            BoletaValidator boletaValidator
    ) {
        this.boletaService = boletaService;
        this.boletaRenderService = boletaRenderService;
        this.boletaPolicy = boletaPolicy;
        this.boletaValidator = boletaValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdf(
            Long idBoleta,
            AuthenticatedUserContext actor
    ) {
        Boleta boleta =
                boletaService
                        .resolverBoletaParaRender(
                                idBoleta
                        );

        boletaPolicy.authorizeGenerarPdf(
                actor,
                boleta
        );

        String html =
                boletaRenderService
                        .renderizarHtml(
                                idBoleta,
                                actor
                        );

        return convertirHtmlAPdf(
                html,
                DEFAULT_BASE_URI
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdfInterno(
            Long idBoleta
    ) {
        String html =
                boletaRenderService
                        .renderizarHtmlInterno(
                                idBoleta
                        );

        return convertirHtmlAPdf(
                html,
                DEFAULT_BASE_URI
        );
    }

    @Override
    public byte[] convertirHtmlAPdf(
            String html,
            String baseUri
    ) {
        boletaValidator.validarHtmlParaPdf(
                html
        );

        String normalizedBaseUri =
                normalizeBaseUri(baseUri);

        try (
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.withHtmlContent(
                    html,
                    normalizedBaseUri
            );

            builder.toStream(
                    outputStream
            );

            builder.run();

            return outputStream.toByteArray();
        } catch (Exception ex) {
            log.error(
                    "Error generando PDF de boleta en memoria. baseUri={}",
                    normalizedBaseUri,
                    ex
            );

            throw new BusinessException(
                    ErrorCodes.INTERNAL_ERROR,
                    "No se pudo generar el PDF de la boleta.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ex
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String construirNombreArchivoPdf(
            Long idBoleta
    ) {
        Boleta boleta =
                boletaService
                        .resolverBoletaParaRender(
                                idBoleta
                        );

        String codigo =
                boleta.getCodigoBoleta();

        if (
                codigo == null
                        || codigo.isBlank()
        ) {
            codigo =
                    "BOLETA-" + idBoleta;
        }

        return codigo
                .trim()
                .replaceAll(
                        "[^A-Za-z0-9._-]",
                        "_"
                )
                .concat(".pdf");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]>
    construirRespuestaPdfInline(
            Long idBoleta,
            AuthenticatedUserContext actor
    ) {
        byte[] pdf =
                generarPdf(
                        idBoleta,
                        actor
                );

        String filename =
                construirNombreArchivoPdf(
                        idBoleta
                );

        return ResponseEntity
                .ok()
                .contentType(
                        MediaType.parseMediaType(
                                PDF_CONTENT_TYPE
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition
                                .inline()
                                .filename(
                                        filename,
                                        StandardCharsets.UTF_8
                                )
                                .build()
                                .toString()
                )
                .contentLength(
                        pdf.length
                )
                .body(pdf);
    }

    private String normalizeBaseUri(
            String baseUri
    ) {
        if (
                baseUri == null
                        || baseUri.isBlank()
        ) {
            return DEFAULT_BASE_URI;
        }

        return baseUri.trim();
    }
}