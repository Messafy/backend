package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.UpdateNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.UpdatedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.UpdateNotePort;
import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Service;

@LogExecution
@Service
public class UpdateNoteUseCase implements UpdateNotePort {

    private final Logger log;
    private final NoteRepository repository;

    public UpdateNoteUseCase(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    @Override
    public Result<UpdatedNoteResponse> update(UpdateNoteRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note update data cannot be null"));
        }

        log.debug("Updating note {} for account {}", request.noteId(), request.requesterId());
        var found = repository.findById(new NoteId(request.noteId()));
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFoundError("note not found: " + request.noteId()));
        }
        Note note = found.get();

        if (!note.isOwnedBy(request.requesterId())) {
            return Result.failure(ApplicationError.forbiddenError("you cannot update this note"));
        }

        try {
            note.rename(request.title());
            note.changeContent(request.content());
            if (request.tags() != null) {
                note.replaceTags(request.tags().stream().map(Tag::new).toList());
            }
        } catch (NoteConflictException e) {
            return Result.failure(ApplicationError.conflictError(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(e.getMessage()));
        }

        repository.save(note);
        log.debug("Updated note {}", note.id().id());
        return Result.success(UpdatedNoteResponse.from(note));
    }
}
