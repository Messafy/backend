package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.DeleteNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.DeletedNoteResponse;
import com.luispiquinrey.backend.share.result.Result;

public interface DeleteNotePort {

    Result<DeletedNoteResponse> delete(DeleteNoteRequest request);
}
