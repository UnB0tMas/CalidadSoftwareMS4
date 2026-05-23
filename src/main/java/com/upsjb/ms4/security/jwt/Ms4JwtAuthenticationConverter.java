// ruta: src/main/java/com/upsjb/ms4/security/jwt/Ms4JwtAuthenticationConverter.java
package com.upsjb.ms4.security.jwt;

import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.shared.exception.UnauthorizedException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Ms4JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtClaimExtractor claimExtractor;
    private final JwtValidationService jwtValidationService;

    public Ms4JwtAuthenticationConverter(JwtClaimExtractor claimExtractor,
                                         JwtValidationService jwtValidationService) {
        this.claimExtractor = claimExtractor;
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        try {
            jwtValidationService.validateAccessToken(source);

            Set<String> authorities = claimExtractor.authorities(source);

            AuthenticatedUserContext principal = new AuthenticatedUserContext(
                    claimExtractor.idUsuarioMs1(source),
                    claimExtractor.username(source),
                    claimExtractor.email(source),
                    claimExtractor.rol(source),
                    authorities,
                    claimExtractor.sid(source),
                    claimExtractor.tokenType(source)
            );

            return new UsernamePasswordAuthenticationToken(
                    principal,
                    source,
                    authorities.stream()
                            .filter(value -> value != null && !value.isBlank())
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toUnmodifiableSet())
            );
        } catch (UnauthorizedException ex) {
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    ex.getMessage(),
                    null
            );
            throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
        }
    }
}