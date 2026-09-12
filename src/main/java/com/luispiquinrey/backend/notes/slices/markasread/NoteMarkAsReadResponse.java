package com.luispiquinrey.backend.notes.slices.markasread;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.share.time.Date;

import java.time.Instant;

public record NoteMarkAsReadResponse(
        String id,
        Instant readAt
) {

    public static NoteMarkAsReadResponse from(Note note) {
        Date readAt = note.readAt();
        return new NoteMarkAsReadResponse(
                note.id().id(),
                readAt == null ? null : readAt.value()
        );
    }
}
