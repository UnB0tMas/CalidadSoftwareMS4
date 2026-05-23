// ruta: src/main/java/com/upsjb/ms4/config/SecurityConfig.java
package com.upsjb.ms4.config;

import com.upsjb.ms4.security.filter.InternalServiceKeyFilter;
import com.upsjb.ms4.security.handler.RestAccessDeniedHandler;
import com.upsjb.ms4.security.handler.RestAuthenticationEntryPoint;
import com.upsjb.ms4.security.jwt.Ms4JwtAuthenticationConverter;
import com.upsjb.ms4.security.roles.SecurityRoles;
import com.upsjb.ms4.shared.constants.ApiPaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final Ms4JwtAuthenticationConverter jwtAuthenticationConverter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final InternalServiceKeyFilter internalServiceKeyFilter;

    public SecurityConfig(Ms4JwtAuthenticationConverter jwtAuthenticationConverter,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler,
                          InternalServiceKeyFilter internalServiceKeyFilter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.internalServiceKeyFilter = internalServiceKeyFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentTypeOptions(Customizer.withDefaults())
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                        )))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        /*
                         * Stripe no usa JWT de usuario.
                         * La firma real se valida en StripeWebhookController/Service
                         * usando el header Stripe-Signature.
                         */
                        .requestMatchers(ApiPaths.STRIPE_WEBHOOK).permitAll()

                        /*
                         * Las rutas internas no exigen JWT porque son servicio a servicio.
                         * Su protección real queda en InternalServiceKeyFilter con X-Internal-Service-Key.
                         */
                        .requestMatchers(
                                ApiPaths.INTERNAL,
                                ApiPaths.INTERNAL + "/**"
                        ).permitAll()

                        .requestMatchers(ApiPaths.ADMIN + "/**")
                        .hasRole(SecurityRoles.ADMIN)

                        .requestMatchers(ApiPaths.EMPLEADO + "/**")
                        .hasAnyRole(SecurityRoles.ADMIN, SecurityRoles.EMPLEADO)

                        .requestMatchers(ApiPaths.CLIENTE + "/**")
                        .hasAnyRole(SecurityRoles.ADMIN, SecurityRoles.CLIENTE)

                        .requestMatchers(ApiPaths.LOOKUPS + "/**")
                        .hasAnyRole(SecurityRoles.ADMIN, SecurityRoles.EMPLEADO, SecurityRoles.CLIENTE)

                        .requestMatchers(ApiPaths.MS4 + "/**")
                        .authenticated()

                        .anyRequest()
                        .denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterBefore(internalServiceKeyFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }
}