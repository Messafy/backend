package com.luispiquinrey.backend.notes.domain.repository;

public record PageQuery(int page, int size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    private static final int UNPAGED_SIZE = -1;

    public PageQuery {
        if (size != UNPAGED_SIZE) {
            if (page < 0) {
                throw new IllegalArgumentException("page cannot be negative");
            }
            if (size < 1 || size > MAX_SIZE) {
                throw new IllegalArgumentException("page size must be between 1 and %d".formatted(MAX_SIZE));
            }
        }
    }

    public static PageQuery of(Integer page, Integer size) {
        return new PageQuery(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size);
    }

    public static PageQuery unpaged() {
        return new PageQuery(0, UNPAGED_SIZE);
    }

    public boolean isPaged() {
        return size != UNPAGED_SIZE;
    }

    public long offset() {
        return isPaged() ? (long) page * size : 0;
    }
}
