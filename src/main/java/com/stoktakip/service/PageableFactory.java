package com.stoktakip.service;

import com.stoktakip.exception.BusinessRuleException;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableFactory {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PageableFactory() {
    }

    public static Pageable sanitize(
        Pageable pageable, Sort defaultSort, Set<String> allowedSortProperties
    ) {
        int page = pageable == null ? 0 : pageable.getPageNumber();
        int requestedSize = pageable == null ? DEFAULT_SIZE : pageable.getPageSize();
        int size = Math.min(requestedSize, MAX_SIZE);
        Sort requestedSort = pageable == null || pageable.getSort().isUnsorted()
            ? defaultSort
            : pageable.getSort();
        requestedSort.forEach(order -> {
            if (!allowedSortProperties.contains(order.getProperty())) {
                throw new BusinessRuleException("Desteklenmeyen sıralama alanı: " + order.getProperty());
            }
        });
        return PageRequest.of(page, size, requestedSort);
    }
}
