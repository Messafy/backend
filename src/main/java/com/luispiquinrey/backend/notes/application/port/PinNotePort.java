package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.PinNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.PinnedNoteResponse;
import com.luispiquinrey.backend.share.result.Result;

public interface PinNotePort {

    Result<PinnedNoteResponse> setPinned(PinNoteRequest request);
}
