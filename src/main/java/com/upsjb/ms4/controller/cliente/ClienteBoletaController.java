// ruta: src/main/java/com/upsjb/ms4/controller/cliente/ClienteBoletaController.java
package com.upsjb.ms4.controller.cliente;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.boleta.filter.BoletaFilterDto;
import com.upsjb.ms4.dto.boleta.request.BoletaReenvioCorreoRequestDto;
import com.upsjb.ms4.dto.boleta.response.BoletaDetailResponseDto;
import com.upsjb.ms4.dto.boleta.response.BoletaResponseDto;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.boleta.BoletaMailService;
import com.upsjb.ms4.service.contract.boleta.BoletaPdfService;
import com.upsjb.ms4.service.contract.boleta.BoletaRenderService;
import com.upsjb.ms4.service.contract.boleta.BoletaService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.CLIENTE_BOLETAS)
@Tag(name = "Cliente - Boletas", description = "Consulta, preview HTML, PDF en vivo y reenvío de boletas.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class ClienteBoletaController {

    private static final MediaType TEXT_HTML_UTF8 = MediaType.parseMediaType("text/html;charset=UTF-8");

    private final BoletaService boletaService;
    private final BoletaRenderService boletaRenderService;
    private final BoletaPdfService boletaPdfService;
    private final BoletaMailService boletaMailService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public ClienteBoletaController(BoletaService boletaService,
                                   BoletaRenderService boletaRenderService,
                                   BoletaPdfService boletaPdfService,
                                   BoletaMailService boletaMailService,
                                   AuthenticatedUserResolver authenticatedUserResolver,
                                   ApiResponseFactory responseFactory) {
        this.boletaService = boletaService;
        this.boletaRenderService = boletaRenderService;
        this.boletaPdfService = boletaPdfService;
        this.boletaMailService = boletaMailService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    @Operation(summary = "Listar boletas del cliente autenticado")
    public ResponseEntity<ApiResponseDto<PageResponseDto<BoletaResponseDto>>> listarMisBoletas(
            @ParameterObject @Valid @ModelAttribute BoletaFilterDto filter,
            @ParameterObject @Valid @ModelAttribute PageRequestDto page,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        PageResponseDto<BoletaResponseDto> data = boletaService.listarCliente(filter, page, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Boletas del cliente consultadas correctamente.",
                servletRequest
        ));
    }

    @GetMapping("/{idBoleta}")
    @Operation(summary = "Obtener detalle de boleta del cliente autenticado")
    public ResponseEntity<ApiResponseDto<BoletaDetailResponseDto>> obtenerMiBoleta(
            @PathVariable @Positive(message = "El idBoleta debe ser positivo.") Long idBoleta,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        BoletaDetailResponseDto data = boletaService.obtenerDetalleCliente(idBoleta, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Boleta consultada correctamente.",
                servletRequest
        ));
    }

    @GetMapping(value = "/{idBoleta}/preview", produces = "text/html;charset=UTF-8")
    @Operation(summary = "Renderizar preview HTML de boleta del cliente")
    public ResponseEntity<String> renderizarPreviewHtml(
            @PathVariable @Positive(message = "El idBoleta debe ser positivo.") Long idBoleta
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        String html = boletaRenderService.renderizarHtml(idBoleta, actor);

        return ResponseEntity.ok()
                .contentType(TEXT_HTML_UTF8)
                .body(html);
    }

    @GetMapping(value = "/{idBoleta}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Generar PDF en vivo de boleta del cliente")
    public ResponseEntity<byte[]> generarPdfEnVivo(
            @PathVariable @Positive(message = "El idBoleta debe ser positivo.") Long idBoleta
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        return boletaPdfService.construirRespuestaPdfInline(idBoleta, actor);
    }

    @PostMapping("/{idBoleta}/reenviar-correo")
    @Operation(summary = "Programar reenvío de boleta por correo")
    public ResponseEntity<ApiResponseDto<CorreoOutboxResponseDto>> reenviarBoleta(
            @PathVariable @Positive(message = "El idBoleta debe ser positivo.") Long idBoleta,
            @Valid @RequestBody(required = false) BoletaReenvioCorreoRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        CorreoOutboxResponseDto data = boletaMailService.programarReenvioBoleta(idBoleta, request, actor);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Correo de boleta programado correctamente.",
                servletRequest
        ));
    }
}