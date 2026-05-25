package com.upsjb.ms4.config;

import com.upsjb.ms4.kafka.probe.KafkaProbeProperties;
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
        CorsProperties.class,
        KafkaProbeProperties.class
})
public class AppPropertiesConfig {
}