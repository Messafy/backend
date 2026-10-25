package com.luispiquinrey.backend.notes.application.dto.response;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.PrivateNote;

import java.time.Instant;

public record CreatedNoteResponse(String id, String type, String status, Instant createdAt) {

    public static CreatedNoteResponse from(Note note) {
        String type = note instanceof PrivateNote ? "PRIVATE" : "SHARED";
        return new CreatedNoteResponse(
                note.id().id(),
                type,
                note.status().name(),
                note.createdAt().value()
        );
    }
}
