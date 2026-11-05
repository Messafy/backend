package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.GetNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.request.ListNotesRequest;
import com.luispiquinrey.backend.notes.application.dto.response.NotePageResponse;
import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.share.result.Result;

public interface GetNotePort {

    Result<NoteResponse> findById(GetNoteRequest request);

    Result<NotePageResponse> findByStatus(ListNotesRequest request);

    Result<NotePageResponse> findActiveByOwner(ListNotesRequest request);

    Result<NotePageResponse> findActiveBySharedWith(ListNotesRequest request);

    Result<NotePageResponse> findActiveByOwnerAndSharedWith(ListNotesRequest request);

    Result<NotePageResponse> findDeletedByOwner(ListNotesRequest request);
}
