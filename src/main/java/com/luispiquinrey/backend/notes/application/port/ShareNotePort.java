package com.luispiquinrey.backend.notes.application.port;

import com.luispiquinrey.backend.notes.application.dto.request.ShareNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.share.result.Result;

public interface ShareNotePort {

    Result<NoteResponse> share(ShareNoteRequest request);
}
