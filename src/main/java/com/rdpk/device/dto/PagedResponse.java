package com.rdpk.device.dto;

import java.util.List;

public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return new PagedResponse<>(
            content,
            page,
            size,
            total,
            totalPages,
            page < totalPages - 1,
            page > 0
        );
    }
}

