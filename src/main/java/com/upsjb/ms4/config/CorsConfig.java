// ruta: src/main/java/com/upsjb/ms4/config/CorsConfig.java
package com.upsjb.ms4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();

        if (properties.enabledSafe()) {
            configuration.setAllowedOrigins(properties.allowedOriginsSafe());
            configuration.setAllowedOriginPatterns(properties.allowedOriginPatternsSafe());
            configuration.setAllowedMethods(properties.allowedMethodsSafe());
            configuration.setAllowedHeaders(properties.allowedHeadersSafe());
            configuration.setExposedHeaders(properties.exposedHeadersSafe());
            configuration.setAllowCredentials(properties.allowCredentialsSafe());
            configuration.setMaxAge(properties.maxAgeSafe());
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}