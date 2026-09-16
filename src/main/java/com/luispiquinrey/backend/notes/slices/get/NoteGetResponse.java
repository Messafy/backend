package com.luispiquinrey.backend.notes.slices.get;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import com.luispiquinrey.backend.notes.domain.PrivateNote;
import com.luispiquinrey.backend.notes.domain.SharedNote;
import com.luispiquinrey.backend.notes.domain.Tag;
import com.luispiquinrey.backend.share.time.Date;

import java.time.Instant;
import java.util.List;

public record NoteGetResponse(
        String id,
        String title,
        String content,
        NoteStatus status,
        String type,
        String ownerId,
        String sharedWith,
        List<String> tags,
        boolean pinned,
        Instant createdAt,
        Instant readAt
) {

    public static NoteGetResponse from(Note note) {
        String type;
        String ownerId;
        String sharedWith;

        if (note instanceof PrivateNote privateNote) {
            type = "PRIVATE";
            ownerId = privateNote.ownerId().id();
            sharedWith = null;
        } else if (note instanceof SharedNote sharedNote) {
            type = "SHARED";
            ownerId = sharedNote.ownerId().id();
            sharedWith = sharedNote.sharedWith().ownerId().id();
        } else {
            throw new IllegalStateException("unsupported note type: " + note.getClass());
        }

        return new NoteGetResponse(
                note.id().id(),
                note.title().title(),
                note.content().content(),
                note.status(),
                type,
                ownerId,
                sharedWith,
                note.tags().stream().map(Tag::tag).toList(),
                note.pinned(),
                toInstant(note.createdAt()),
                toInstant(note.readAt())
        );
    }

    private static Instant toInstant(Date date) {
        return date == null ? null : date.value();
    }
}
