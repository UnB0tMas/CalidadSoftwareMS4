// ruta: src/main/java/com/upsjb/ms4/controller/cliente/ClientePagoStripeController.java
package com.upsjb.ms4.controller.cliente;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.lookup.LookupItemResponseDto;
import com.upsjb.ms4.dto.pago.request.PagoStripeOnlineRequestDto;
import com.upsjb.ms4.dto.pago.response.StripePaymentIntentResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.pago.StripePaymentService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.CLIENTE_PAGOS_STRIPE)
@Tag(name = "Cliente - Pagos Stripe", description = "PaymentIntent online en Stripe Sandbox.")
@SecurityRequirement(name = SwaggerSecurityConfig.BEARER_AUTH)
public class ClientePagoStripeController {

    private final StripePaymentService stripePaymentService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ApiResponseFactory responseFactory;

    public ClientePagoStripeController(StripePaymentService stripePaymentService,
                                       AuthenticatedUserResolver authenticatedUserResolver,
                                       ApiResponseFactory responseFactory) {
        this.stripePaymentService = stripePaymentService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/payment-intent")
    @Operation(summary = "Crear PaymentIntent online en Stripe Sandbox")
    public ResponseEntity<ApiResponseDto<StripePaymentIntentResponseDto>> crearPaymentIntentOnline(
            @Valid @RequestBody PagoStripeOnlineRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();
        StripePaymentIntentResponseDto data = stripePaymentService.crearPaymentIntentOnline(request, actor);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseFactory.created(
                data,
                "PaymentIntent online creado correctamente en Stripe Sandbox.",
                servletRequest
        ));
    }

    @GetMapping("/config")
    @Operation(summary = "Obtener configuración pública de Stripe Sandbox")
    public ResponseEntity<ApiResponseDto<LookupItemResponseDto>> obtenerConfiguracionPublicaStripeSandbox(
            HttpServletRequest servletRequest
    ) {
        authenticatedUserResolver.current();
        LookupItemResponseDto data = stripePaymentService.obtenerPublishableKeySandbox();

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Configuración pública de Stripe Sandbox consultada correctamente.",
                servletRequest
        ));
    }
}