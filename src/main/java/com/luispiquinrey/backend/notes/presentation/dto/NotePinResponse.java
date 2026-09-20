package com.luispiquinrey.backend.notes.presentation.dto;

import com.luispiquinrey.backend.notes.domain.model.Note;

public record NotePinResponse(String id, boolean pinned) {
    public static NotePinResponse from(Note note) {
        return new NotePinResponse(note.id().id(), note.pinned());
    }
}
