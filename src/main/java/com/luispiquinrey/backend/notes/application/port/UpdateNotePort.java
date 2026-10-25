package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.UpdateNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.UpdatedNoteResponse;
import com.luispiquinrey.backend.share.result.Result;

public interface UpdateNotePort {

    Result<UpdatedNoteResponse> update(UpdateNoteRequest request);
}
