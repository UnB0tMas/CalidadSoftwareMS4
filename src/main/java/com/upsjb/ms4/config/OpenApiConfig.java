// ruta: src/main/java/com/upsjb/ms4/config/OpenApiConfig.java
package com.upsjb.ms4.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ms4OpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS4 - Ventas y Facturación")
                        .version("v1")
                        .description("""
                                API del microservicio MS4 para ventas, pagos, boletas, caja, reportes,
                                outbox, contingencia, snapshots, webhooks Stripe y auditoría funcional.
                                MS4 opera detrás del API Gateway y valida JWT emitidos por MS1.
                                """)
                        .contact(new Contact()
                                .name("CalidadSoftware")
                                .email("soporte@calidadsoftware.local"))
                        .license(new License()
                                .name("Uso interno académico")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway local"),
                        new Server().url("http://localhost:8084").description("MS4 directo local")
                ))
                .tags(List.of(
                        new Tag().name("Cliente - Catálogo").description("Consulta de snapshots vendibles para compra online."),
                        new Tag().name("Cliente - Ventas").description("Ventas online del cliente autenticado."),
                        new Tag().name("Cliente - Pagos Stripe").description("PaymentIntent online en Stripe Sandbox."),
                        new Tag().name("Cliente - Boletas").description("Consulta, preview HTML, PDF en vivo y reenvío de boletas."),
                        new Tag().name("Empleado - Caja").description("Apertura, cierre, ajustes y movimientos de caja."),
                        new Tag().name("Empleado - Ventas").description("Ventas físicas y pagos presenciales."),
                        new Tag().name("Admin - Configuración").description("Configuración empresarial, tributaria y series de boleta."),
                        new Tag().name("Admin - Outbox").description("Gestión de eventos Kafka y correo outbox."),
                        new Tag().name("Admin - Auditoría").description("Consulta de auditoría funcional."),
                        new Tag().name("Internal - Stock Sync").description("Rutas internas MS3 ↔ MS4 protegidas por service key."),
                        new Tag().name("Webhook - Stripe").description("Recepción idempotente de eventos Stripe Sandbox.")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("MS4 opera mediante API Gateway y no expone autenticación propia."));
    }
}