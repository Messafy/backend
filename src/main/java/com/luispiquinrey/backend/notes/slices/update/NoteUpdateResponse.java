package com.luispiquinrey.backend.notes.slices.update;

import com.luispiquinrey.backend.notes.domain.Note;

public record NoteUpdateResponse(
        String id,
        String title,
        String content
) {
    public static NoteUpdateResponse from(Note note) {
        return new NoteUpdateResponse(
                note.id().id(),
                note.title().title(),
                note.content().content()
        );
    }
}
