package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.MarkNoteAsReadRequest;
import com.luispiquinrey.backend.notes.application.dto.response.ReadNoteResponse;
import com.luispiquinrey.backend.notes.application.port.MarkNoteAsReadPort;
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
public class MarkNoteAsReadUseCase implements MarkNoteAsReadPort {

    private final Logger log;
    private final NoteRepository repository;

    public MarkNoteAsReadUseCase(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    @Override
    public Result<ReadNoteResponse> markAsRead(MarkNoteAsReadRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note read data cannot be null"));
        }

        log.debug("Marking note {} as read for account {}", request.noteId(), request.readerId());
        var found = repository.findById(new NoteId(request.noteId()));
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFoundError("note not found: " + request.noteId()));
        }
        Note note = found.get();

        if (!note.canAccess(request.readerId())) {
            return Result.failure(ApplicationError.forbiddenError(
                    "only the owner or the shared recipient can mark a note as read"
            ));
        }

        try {
            note.markAsRead();
        } catch (NoteConflictException e) {
            return Result.failure(ApplicationError.conflictError(e.getMessage()));
        }

        repository.save(note);
        log.debug("Marked note {} as read", note.id().id());
        return Result.success(ReadNoteResponse.from(note));
    }
}
