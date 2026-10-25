package com.luispiquinrey.backend.notes.application.dto.request;

import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.share.identity.UserId;

public record MarkNoteAsReadRequest(String noteId, String readerId) {
    public MarkNoteAsReadRequest {
        new NoteId(noteId);
        new UserId(readerId);
    }
}
