package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.PinNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.PinnedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.PinNotePort;
import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Service;

@LogExecution
@Service
public class PinNoteUseCase implements PinNotePort {

    private final Logger log;
    private final NoteRepository repository;

    public PinNoteUseCase(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    @Override
    public Result<PinnedNoteResponse> setPinned(PinNoteRequest request) {
        if (request == null || request.pinned() == null) {
            return Result.failure(ApplicationError.validationError("note pin data cannot be null"));
        }

        log.debug("Setting pinned={} on note {} for account {}",
                request.pinned(), request.noteId(), request.requesterId());
        var found = repository.findById(new NoteId(request.noteId()));
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFoundError("note not found: " + request.noteId()));
        }
        Note note = found.get();

        if (!note.isOwnedBy(request.requesterId())) {
            return Result.failure(ApplicationError.forbiddenError("you cannot pin this note"));
        }

        try {
            note.setPinned(request.pinned());
        } catch (NoteConflictException e) {
            return Result.failure(ApplicationError.conflictError(e.getMessage()));
        }

        repository.save(note);
        log.debug("Set pinned={} on note {}", request.pinned(), note.id().id());
        return Result.success(PinnedNoteResponse.from(note));
    }
}
