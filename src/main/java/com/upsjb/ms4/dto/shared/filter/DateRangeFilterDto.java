// ruta: src/main/java/com/upsjb/ms4/dto/shared/filter/DateRangeFilterDto.java
package com.upsjb.ms4.dto.shared.filter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DateRangeFilterDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        LocalDateTime fechaHoraDesde,
        LocalDateTime fechaHoraHasta
) {
}