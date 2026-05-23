// ruta: src/main/java/com/upsjb/ms4/validator/LookupValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import org.springframework.stereotype.Component;

@Component
public class LookupValidator extends ValidatorSupport {

    public void validarFiltro(LookupFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");

        if (filter.limit() != null && (filter.limit() < 1 || filter.limit() > 50)) {
            fail("El límite de resultados debe estar entre 1 y 50.");
        }
    }
}