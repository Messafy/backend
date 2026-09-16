package com.luispiquinrey.backend.notes.slices.pin;

import com.luispiquinrey.backend.notes.domain.Note;

public record NotePinResponse(String id, boolean pinned) {
    public static NotePinResponse from(Note note) {
        return new NotePinResponse(note.id().id(), note.pinned());
    }
}
