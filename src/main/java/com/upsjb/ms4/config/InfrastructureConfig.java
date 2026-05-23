// ruta: src/main/java/com/upsjb/ms4/config/InfrastructureConfig.java
package com.upsjb.ms4.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableKafka
@EnableScheduling
public class InfrastructureConfig {
}