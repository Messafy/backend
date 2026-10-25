package com.luispiquinrey.backend.notes.application.dto.response;

import com.luispiquinrey.backend.notes.domain.model.Note;

public record UpdatedNoteResponse(String id, String title, String content) {

    public static UpdatedNoteResponse from(Note note) {
        return new UpdatedNoteResponse(
                note.id().id(),
                note.title().title(),
                note.content().content()
        );
    }
}
