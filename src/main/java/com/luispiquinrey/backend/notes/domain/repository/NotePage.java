package com.luispiquinrey.backend.notes.domain.repository;

import com.luispiquinrey.backend.notes.domain.model.Note;

import java.util.List;
import java.util.Objects;

public record NotePage(List<Note> items, PageQuery query, long totalItems) {

    public NotePage {
        Objects.requireNonNull(query, "query cannot be null");
        items = items == null ? List.of() : List.copyOf(items);
        if (totalItems < items.size()) {
            throw new IllegalArgumentException("total items cannot be lower than the page size");
        }
    }
}
