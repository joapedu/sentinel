package com.sentinel.core.domain.common;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResult<T> of(List<T> items, int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(items, page, size, totalElements, totalPages);
    }
}
