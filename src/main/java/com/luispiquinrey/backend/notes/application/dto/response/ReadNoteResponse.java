package com.luispiquinrey.backend.notes.application.dto.response;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.share.time.Date;

import java.time.Instant;

public record ReadNoteResponse(String id, Instant readAt) {

    public static ReadNoteResponse from(Note note) {
        Date readAt = note.readAt();
        return new ReadNoteResponse(note.id().id(), readAt == null ? null : readAt.value());
    }
}
