// ruta: src/main/java/com/upsjb/ms4/dto/shared/PageResponseDto.java
package com.upsjb.ms4.dto.shared;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty,
        String sortBy,
        String sortDirection
) {
}