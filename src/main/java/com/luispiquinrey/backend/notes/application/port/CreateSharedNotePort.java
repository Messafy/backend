package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.CreateSharedNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse;
import com.luispiquinrey.backend.share.result.Result;

public interface CreateSharedNotePort {

    Result<CreatedNoteResponse> create(CreateSharedNoteRequest request);
}
