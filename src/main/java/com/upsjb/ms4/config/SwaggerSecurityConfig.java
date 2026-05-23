// ruta: src/main/java/com/upsjb/ms4/config/SwaggerSecurityConfig.java
package com.upsjb.ms4.config;

import com.upsjb.ms4.shared.constants.HeaderNames;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerSecurityConfig {

    public static final String BEARER_AUTH = "bearerAuth";
    public static final String INTERNAL_KEY_AUTH = "internalServiceKey";
    public static final String STRIPE_SIGNATURE_AUTH = "stripeSignature";

    @Bean
    public OpenApiCustomizer ms4SecurityOpenApiCustomizer() {
        return this::applySecuritySchemes;
    }

    private void applySecuritySchemes(OpenAPI openApi) {
        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        components
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .name(BEARER_AUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT access token emitido por MS1."))
                .addSecuritySchemes(INTERNAL_KEY_AUTH, new SecurityScheme()
                        .name(HeaderNames.INTERNAL_SERVICE_KEY)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("Clave interna para endpoints /api/internal/ms4/**. No debe usarse desde Angular."))
                .addSecuritySchemes(STRIPE_SIGNATURE_AUTH, new SecurityScheme()
                        .name(HeaderNames.STRIPE_SIGNATURE)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("Firma enviada por Stripe para validar el webhook."));

        openApi.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}