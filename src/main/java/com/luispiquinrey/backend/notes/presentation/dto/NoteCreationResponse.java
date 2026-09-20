package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.model.PrivateNote;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;

import java.time.Instant;

public record NoteCreationResponse(
        String id,
        NoteCreationRequest.NoteType type,
        NoteStatus status,
        Instant createdAt
) {

    public static NoteCreationResponse from(Note note) {
        NoteCreationRequest.NoteType type = switch (note) {
            case PrivateNote ignored -> NoteCreationRequest.NoteType.PRIVATE;
            case SharedNote ignored -> NoteCreationRequest.NoteType.SHARED;
            default -> throw new IllegalStateException("unsupported note type: " + note.getClass());
        };

        return new NoteCreationResponse(
                note.id().id(),
                type,
                note.status(),
                note.createdAt().value()
        );
    }
}
