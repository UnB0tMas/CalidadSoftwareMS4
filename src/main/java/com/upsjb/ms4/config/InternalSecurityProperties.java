// ruta: src/main/java/com/upsjb/ms4/config/InternalSecurityProperties.java
package com.upsjb.ms4.config;

import com.upsjb.ms4.shared.constants.HeaderNames;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "ms4.security.internal")
public record InternalSecurityProperties(
        Boolean enabled,
        String headerName,
        String serviceKey
) {

    public boolean enabledSafe() {
        return enabled == null || enabled;
    }

    public String headerNameSafe() {
        return headerName == null || headerName.isBlank()
                ? HeaderNames.INTERNAL_SERVICE_KEY
                : headerName.trim();
    }

    public String serviceKeySafe() {
        return serviceKey == null ? null : serviceKey.trim();
    }

    public byte[] serviceKeyBytes() {
        String value = serviceKeySafe();
        return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
    }

    public boolean configured() {
        return serviceKeySafe() != null && !serviceKeySafe().isBlank();
    }
}