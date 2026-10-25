package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.GetNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.request.ListNotesRequest;
import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.share.result.Result;

import java.util.List;

public interface GetNotePort {

    Result<NoteResponse> findById(GetNoteRequest request);

    Result<List<NoteResponse>> findByStatus(ListNotesRequest request);

    Result<List<NoteResponse>> findActiveByOwner(ListNotesRequest request);

    Result<List<NoteResponse>> findActiveBySharedWith(ListNotesRequest request);

    Result<List<NoteResponse>> findActiveByOwnerAndSharedWith(ListNotesRequest request);

    Result<List<NoteResponse>> findDeletedByOwner(ListNotesRequest request);
}
