package com.upsjb.ms4.controller.webhook;

import com.upsjb.ms4.config.SwaggerSecurityConfig;
import com.upsjb.ms4.dto.pago.response.StripeWebhookProcessResponseDto;
import com.upsjb.ms4.dto.shared.ApiResponseDto;
import com.upsjb.ms4.service.contract.pago.StripeWebhookService;
import com.upsjb.ms4.shared.constants.ApiPaths;
import com.upsjb.ms4.shared.constants.HeaderNames;
import com.upsjb.ms4.shared.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.STRIPE_WEBHOOK)
@Tag(name = "Webhook - Stripe", description = "Recepción idempotente de eventos Stripe Sandbox.")
@SecurityRequirement(name = SwaggerSecurityConfig.STRIPE_SIGNATURE_AUTH)
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;
    private final ApiResponseFactory responseFactory;

    public StripeWebhookController(StripeWebhookService stripeWebhookService,
                                   ApiResponseFactory responseFactory) {
        this.stripeWebhookService = stripeWebhookService;
        this.responseFactory = responseFactory;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Procesar webhook firmado de Stripe Sandbox")
    public ResponseEntity<ApiResponseDto<StripeWebhookProcessResponseDto>> procesarWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(HeaderNames.STRIPE_SIGNATURE) String stripeSignature,
            HttpServletRequest servletRequest
    ) {
        StripeWebhookProcessResponseDto data =
                stripeWebhookService.procesarWebhook(rawPayload, stripeSignature);

        return ResponseEntity.ok(responseFactory.ok(
                data,
                "Webhook Stripe procesado correctamente.",
                servletRequest
        ));
    }
}