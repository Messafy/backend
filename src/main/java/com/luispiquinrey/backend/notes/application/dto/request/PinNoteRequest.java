package com.luispiquinrey.backend.notes.application.dto.request;

import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.share.identity.UserId;

public record PinNoteRequest(String noteId, String requesterId, Boolean pinned) {
    public PinNoteRequest {
        new NoteId(noteId);
        new UserId(requesterId);
        if (pinned == null) {
            throw new IllegalArgumentException("note pin data cannot be null");
        }
    }
}
