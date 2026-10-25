package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.CreatePrivateNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse;
import com.luispiquinrey.backend.share.result.Result;

public interface CreatePrivateNotePort {

    Result<CreatedNoteResponse> create(CreatePrivateNoteRequest request);
}
