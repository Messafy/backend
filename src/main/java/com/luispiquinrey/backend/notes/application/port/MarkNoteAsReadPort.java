package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.MarkNoteAsReadRequest;
import com.luispiquinrey.backend.notes.application.dto.response.ReadNoteResponse;
import com.luispiquinrey.backend.share.result.Result;

public interface MarkNoteAsReadPort {

    Result<ReadNoteResponse> markAsRead(MarkNoteAsReadRequest request);
}
