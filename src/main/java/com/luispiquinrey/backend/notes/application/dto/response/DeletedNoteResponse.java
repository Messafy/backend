package com.luispiquinrey.backend.notes.application.dto.response;

import com.luispiquinrey.backend.notes.domain.model.Note;

public record DeletedNoteResponse(String id) {

    public static DeletedNoteResponse from(Note note) {
        return new DeletedNoteResponse(note.id().id());
    }
}
