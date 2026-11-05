package com.luispiquinrey.backend.notes.application.dto.response;

import com.luispiquinrey.backend.notes.domain.repository.NotePage;

import java.util.List;

public record NotePageResponse(List<NoteResponse> items, int page, int size, long totalItems) {

    public NotePageResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static NotePageResponse from(NotePage page) {
        List<NoteResponse> items = page.items().stream().map(NoteResponse::from).toList();
        int size = page.query().isPaged() ? page.query().size() : items.size();
        return new NotePageResponse(items, page.query().page(), size, page.totalItems());
    }
}
