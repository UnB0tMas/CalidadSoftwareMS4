// ruta: src/main/java/com/upsjb/ms4/config/AppPropertiesConfig.java
package com.upsjb.ms4.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        KafkaTopicProperties.class,
        OutboxProperties.class,
        CorreoOutboxProperties.class,
        StripeProperties.class,
        CloudinaryProperties.class,
        InternalSecurityProperties.class,
        JwtValidationProperties.class,
        CorsProperties.class
})
public class AppPropertiesConfig {
}