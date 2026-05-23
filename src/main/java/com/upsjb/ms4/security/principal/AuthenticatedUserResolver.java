// ruta: src/main/java/com/upsjb/ms4/security/principal/AuthenticatedUserResolver.java
package com.upsjb.ms4.security.principal;

import com.upsjb.ms4.security.jwt.JwtClaimExtractor;
import com.upsjb.ms4.security.jwt.JwtValidationService;
import com.upsjb.ms4.shared.exception.UnauthorizedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserResolver {

    private final JwtClaimExtractor claimExtractor;
    private final JwtValidationService jwtValidationService;

    public AuthenticatedUserResolver(JwtClaimExtractor claimExtractor,
                                     JwtValidationService jwtValidationService) {
        this.claimExtractor = claimExtractor;
        this.jwtValidationService = jwtValidationService;
    }

    public AuthenticatedUserContext current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Usuario no autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUserContext context) {
            return context;
        }

        if (principal instanceof Jwt jwt) {
            return fromJwt(jwt);
        }

        Object credentials = authentication.getCredentials();

        if (credentials instanceof Jwt jwt) {
            return fromJwt(jwt);
        }

        throw new UnauthorizedException("No se pudo resolver el usuario autenticado.");
    }

    public Long currentUserIdMs1() {
        return current().idUsuarioMs1();
    }

    public String currentUsername() {
        return current().username();
    }

    public String currentEmail() {
        return current().email();
    }

    public String currentRole() {
        return current().rol();
    }

    private AuthenticatedUserContext fromJwt(Jwt jwt) {
        jwtValidationService.validateAccessToken(jwt);

        return new AuthenticatedUserContext(
                claimExtractor.idUsuarioMs1(jwt),
                claimExtractor.username(jwt),
                claimExtractor.email(jwt),
                claimExtractor.rol(jwt),
                claimExtractor.authorities(jwt),
                claimExtractor.sid(jwt),
                claimExtractor.tokenType(jwt)
        );
    }
}