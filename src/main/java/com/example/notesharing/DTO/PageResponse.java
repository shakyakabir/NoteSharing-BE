package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Generic paginated response wrapper for admin list endpoints. Built from a Spring Data
 * {@code Page} so the frontend gets the rows plus the totals it needs to render pagination.
 */
@Getter
@Setter
@Builder
public class PageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
