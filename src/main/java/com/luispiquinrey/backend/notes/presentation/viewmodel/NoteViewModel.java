package com.luispiquinrey.backend.notes.presentation.viewmodel;

import java.util.List;

public record NoteViewModel(
        String id,
        String title,
        String content,
        String status,
        String type,
        String ownerId,
        String sharedWith,
        List<String> tags,
        boolean pinned,
        String createdAt,
        String readAt
) {
}
