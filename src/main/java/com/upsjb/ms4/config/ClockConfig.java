// ruta: src/main/java/com/upsjb/ms4/config/ClockConfig.java
package com.upsjb.ms4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    public static final String DEFAULT_ZONE_ID = "America/Lima";

    @Bean
    public ZoneId applicationZoneId() {
        return ZoneId.of(DEFAULT_ZONE_ID);
    }

    @Bean
    public Clock clock(ZoneId applicationZoneId) {
        return Clock.system(applicationZoneId);
    }
}