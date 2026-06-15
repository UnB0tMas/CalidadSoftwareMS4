package com.upsjb.ms4.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    private static final JsonInclude.Value NON_NULL_INCLUSION =
            JsonInclude.Value.construct(
                    JsonInclude.Include.NON_NULL,
                    JsonInclude.Include.NON_NULL
            );

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(
                        new JavaTimeModule()
                )
                .defaultPropertyInclusion(
                        NON_NULL_INCLUSION
                )
                .disable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                )
                .disable(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                )
                .disable(
                        MapperFeature.DEFAULT_VIEW_INCLUSION
                )
                .build();
    }
}