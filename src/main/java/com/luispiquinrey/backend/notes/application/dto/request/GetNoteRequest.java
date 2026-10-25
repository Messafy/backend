package com.luispiquinrey.backend.notes.application.dto.request;

import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.share.identity.UserId;

public record GetNoteRequest(String noteId, String requesterId) {
    public GetNoteRequest {
        new NoteId(noteId);
        new UserId(requesterId);
    }
}
