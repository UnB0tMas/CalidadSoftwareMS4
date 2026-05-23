// ruta: src/main/java/com/upsjb/ms4/security/jwt/JwtValidationService.java
package com.upsjb.ms4.security.jwt;

import com.upsjb.ms4.config.JwtValidationProperties;
import com.upsjb.ms4.security.roles.SecurityRoles;
import com.upsjb.ms4.shared.exception.UnauthorizedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Service
public class JwtValidationService {

    private final JwtClaimExtractor claimExtractor;
    private final JwtValidationProperties properties;
    private final Clock clock;

    public JwtValidationService(JwtClaimExtractor claimExtractor,
                                JwtValidationProperties properties,
                                Clock clock) {
        this.claimExtractor = claimExtractor;
        this.properties = properties;
        this.clock = clock;
    }

    public void validateAccessToken(Jwt jwt) {
        if (!properties.enabledSafe()) {
            return;
        }

        if (jwt == null) {
            throw new UnauthorizedException("Token JWT ausente.");
        }

        validateTemporalClaims(jwt);
        validateIssuer(jwt);
        validateAudience(jwt);
        validatePrincipalClaims(jwt);
        validateRole(jwt);
        validateTokenType(jwt);
    }

    private void validateTemporalClaims(Jwt jwt) {
        Instant now = Instant.now(clock);

        if (jwt.getExpiresAt() != null && now.isAfter(jwt.getExpiresAt())) {
            throw new UnauthorizedException("El token JWT está expirado.");
        }

        if (jwt.getNotBefore() != null && now.isBefore(jwt.getNotBefore())) {
            throw new UnauthorizedException("El token JWT aún no es válido.");
        }
    }

    private void validateIssuer(Jwt jwt) {
        String requiredIssuer = properties.requiredIssuerSafe();

        if (requiredIssuer == null) {
            return;
        }

        if (jwt.getIssuer() == null || !requiredIssuer.equals(jwt.getIssuer().toString())) {
            throw new UnauthorizedException("El issuer del token JWT no es válido.");
        }
    }

    private void validateAudience(Jwt jwt) {
        String requiredAudience = properties.requiredAudienceSafe();

        if (requiredAudience == null) {
            return;
        }

        if (jwt.getAudience() == null || jwt.getAudience().stream().noneMatch(requiredAudience::equals)) {
            throw new UnauthorizedException("El audience del token JWT no es válido para MS4.");
        }
    }

    private void validatePrincipalClaims(Jwt jwt) {
        Long idUsuarioMs1 = claimExtractor.idUsuarioMs1(jwt);

        if (idUsuarioMs1 == null || idUsuarioMs1 <= 0) {
            throw new UnauthorizedException("El token no contiene id_usuario_ms1 válido.");
        }

        if (isBlank(claimExtractor.username(jwt))) {
            throw new UnauthorizedException("El token no contiene username.");
        }

        if (properties.requireEmailSafe() && isBlank(claimExtractor.email(jwt))) {
            throw new UnauthorizedException("El token no contiene email.");
        }
    }

    private void validateRole(Jwt jwt) {
        String role = claimExtractor.rol(jwt);

        if (isBlank(role)) {
            throw new UnauthorizedException("El token no contiene rol.");
        }

        String normalizedRole = SecurityRoles.normalize(role);

        if (!properties.allowedRolesSafe().contains(normalizedRole)) {
            throw new UnauthorizedException("El rol del token no está autorizado para MS4.");
        }
    }

    private void validateTokenType(Jwt jwt) {
        String tokenType = claimExtractor.tokenType(jwt);

        if (properties.requireTokenTypeSafe() && isBlank(tokenType)) {
            throw new UnauthorizedException("El token no contiene tipo de token.");
        }

        if (isBlank(tokenType)) {
            return;
        }

        String normalized = tokenType.trim().toLowerCase(Locale.ROOT);

        if (!properties.acceptedTokenTypesSafe().contains(normalized)) {
            throw new UnauthorizedException("El token no es de tipo access.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}