package com.luispiquinrey.backend.notes.application.dto.request;

import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.PageQuery;
import com.luispiquinrey.backend.share.identity.UserId;

public record ListNotesRequest(String requesterId, NoteStatus status, String sharedWith, PageQuery page) {
    public ListNotesRequest {
        new UserId(requesterId);
        if (sharedWith != null) {
            new UserId(sharedWith);
        }
        page = page == null ? PageQuery.unpaged() : page;
    }

    public ListNotesRequest(String requesterId, NoteStatus status, String sharedWith) {
        this(requesterId, status, sharedWith, PageQuery.unpaged());
    }
}
