package com.luispiquinrey.backend.notes.application.dto.response;

import com.luispiquinrey.backend.notes.domain.model.Note;

public record PinnedNoteResponse(String id, boolean pinned) {

    public static PinnedNoteResponse from(Note note) {
        return new PinnedNoteResponse(note.id().id(), note.pinned());
    }
}
