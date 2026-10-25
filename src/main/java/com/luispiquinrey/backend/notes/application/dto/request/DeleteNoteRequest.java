package com.luispiquinrey.backend.notes.application.dto.request;

import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.share.identity.UserId;

public record DeleteNoteRequest(String noteId, String requesterId) {
    public DeleteNoteRequest {
        new NoteId(noteId);
        new UserId(requesterId);
    }
}
