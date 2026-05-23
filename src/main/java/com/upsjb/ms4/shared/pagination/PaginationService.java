// ruta: src/main/java/com/upsjb/ms4/shared/pagination/PaginationService.java
package com.upsjb.ms4.shared.pagination;

import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.shared.constants.Ms4Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
public class PaginationService {

    private static final Pattern SAFE_SORT_PROPERTY =
            Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.]*$");

    public Pageable toPageable(PageRequestDto request) {
        int page = safePage(request);
        int size = safeSize(request);
        String sortBy = safeSortBy(request);
        Sort.Direction direction = safeDirection(request);

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    public Pageable toPageable(PageRequestDto request, String defaultSortBy) {
        int page = safePage(request);
        int size = safeSize(request);
        String sortBy = safeSortBy(request, defaultSortBy);
        Sort.Direction direction = safeDirection(request);

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    public <E, D> PageResponseDto<D> toPageResponse(Page<E> page, Function<E, D> mapper) {
        if (page == null) {
            return empty();
        }

        Function<E, D> safeMapper = mapper == null ? value -> null : mapper;
        List<D> content = page.getContent().stream().map(safeMapper).toList();

        String sortBy = page.getSort().stream()
                .findFirst()
                .map(Sort.Order::getProperty)
                .orElse(Ms4Constants.DEFAULT_SORT_BY);

        String sortDirection = page.getSort().stream()
                .findFirst()
                .map(order -> order.getDirection().name())
                .orElse(Ms4Constants.DEFAULT_SORT_DIRECTION);

        return new PageResponseDto<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty(),
                sortBy,
                sortDirection
        );
    }

    public <T> PageResponseDto<T> empty() {
        return new PageResponseDto<>(
                List.of(),
                Ms4Constants.DEFAULT_PAGE,
                Ms4Constants.DEFAULT_PAGE_SIZE,
                0,
                0,
                true,
                true,
                true,
                Ms4Constants.DEFAULT_SORT_BY,
                Ms4Constants.DEFAULT_SORT_DIRECTION
        );
    }

    private int safePage(PageRequestDto request) {
        int page = request != null && request.page() != null
                ? request.page()
                : Ms4Constants.DEFAULT_PAGE;

        return Math.max(page, 0);
    }

    private int safeSize(PageRequestDto request) {
        int size = request != null && request.size() != null
                ? request.size()
                : Ms4Constants.DEFAULT_PAGE_SIZE;

        return Math.min(Math.max(size, 1), Ms4Constants.MAX_PAGE_SIZE);
    }

    private String safeSortBy(PageRequestDto request) {
        return safeSortBy(request, Ms4Constants.DEFAULT_SORT_BY);
    }

    private String safeSortBy(PageRequestDto request, String defaultSortBy) {
        String fallback = isSafeSort(defaultSortBy) ? defaultSortBy.trim() : Ms4Constants.DEFAULT_SORT_BY;

        if (request == null || request.sortBy() == null || request.sortBy().isBlank()) {
            return fallback;
        }

        String candidate = request.sortBy().trim();
        return isSafeSort(candidate) ? candidate : fallback;
    }

    private Sort.Direction safeDirection(PageRequestDto request) {
        String direction = request != null && request.sortDirection() != null
                ? request.sortDirection().trim()
                : Ms4Constants.DEFAULT_SORT_DIRECTION;

        return "ASC".equals(direction.toUpperCase(Locale.ROOT))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
    }

    private boolean isSafeSort(String value) {
        return value != null
                && !value.isBlank()
                && value.length() <= 80
                && SAFE_SORT_PROPERTY.matcher(value.trim()).matches();
    }
}