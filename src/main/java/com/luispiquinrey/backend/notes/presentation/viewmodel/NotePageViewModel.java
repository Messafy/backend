package com.luispiquinrey.backend.notes.presentation.viewmodel;

import java.util.List;

public record NotePageViewModel(
        List<NoteViewModel> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
    public NotePageViewModel {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
