package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.model.PrivateNote;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;

import java.time.Instant;

public record NoteCreationResponse(
        String id,
        String type,
        NoteStatus status,
        Instant createdAt
) {

    public static NoteCreationResponse from(Note note) {
        String type;
        if (note instanceof PrivateNote) {
            type = "PRIVATE";
        } else if (note instanceof SharedNote) {
            type = "SHARED";
        } else {
            throw new IllegalStateException("unsupported note type: " + note.getClass());
        }

        return new NoteCreationResponse(
                note.id().id(),
                type,
                note.status(),
                note.createdAt().value()
        );
    }
}