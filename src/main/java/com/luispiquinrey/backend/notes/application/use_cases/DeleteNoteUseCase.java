package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.DeleteNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.DeletedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.DeleteNotePort;
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
public class DeleteNoteUseCase implements DeleteNotePort {

    private final Logger log;
    private final NoteRepository repository;

    public DeleteNoteUseCase(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    @Override
    public Result<DeletedNoteResponse> delete(DeleteNoteRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note deletion data cannot be null"));
        }

        log.debug("Deleting note {} for account {}", request.noteId(), request.requesterId());
        var found = repository.findById(new NoteId(request.noteId()));
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFoundError("note not found: " + request.noteId()));
        }
        Note note = found.get();

        if (!note.isOwnedBy(request.requesterId())) {
            return Result.failure(ApplicationError.forbiddenError("you cannot delete this note"));
        }

        try {
            note.delete();
        } catch (NoteConflictException e) {
            return Result.failure(ApplicationError.conflictError(e.getMessage()));
        }

        repository.save(note);
        log.debug("Deleted note {}", note.id().id());
        return Result.success(DeletedNoteResponse.from(note));
    }
}
