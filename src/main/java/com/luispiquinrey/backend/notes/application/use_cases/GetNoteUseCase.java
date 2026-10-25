package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.GetNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.request.ListNotesRequest;
import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.notes.application.port.GetNotePort;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@LogExecution
@Service
public class GetNoteUseCase implements GetNotePort {

    private final Logger log;
    private final NoteRepository repository;

    public GetNoteUseCase(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    @Override
    public Result<NoteResponse> findById(GetNoteRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note lookup data cannot be null"));
        }

        log.debug("Loading note {}", request.noteId());
        return repository.findById(new NoteId(request.noteId()))
                .filter(note -> note.canAccess(request.requesterId()))
                .map(note -> Result.success(NoteResponse.from(note)))
                .orElseGet(() -> Result.failure(ApplicationError.notFoundError(
                        "note not found: " + request.noteId()
                )));
    }

    @Override
    public Result<List<NoteResponse>> findByStatus(ListNotesRequest request) {
        if (request == null || request.status() == null) {
            return Result.failure(ApplicationError.validationError("note status cannot be null"));
        }

        log.debug("Loading notes with status {}", request.status());
        List<NoteResponse> notes = repository.findByStatus(request.status().name()).stream()
                .filter(note -> note.canAccess(request.requesterId()))
                .map(NoteResponse::from)
                .toList();
        return Result.success(notes);
    }

    @Override
    public Result<List<NoteResponse>> findActiveByOwner(ListNotesRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note lookup data cannot be null"));
        }

        log.debug("Loading active notes for account {}", request.requesterId());
        return Result.success(responses(repository.findActiveByOwner(request.requesterId())));
    }

    @Override
    public Result<List<NoteResponse>> findActiveBySharedWith(ListNotesRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note lookup data cannot be null"));
        }

        log.debug("Loading active notes shared with {}", request.requesterId());
        return Result.success(responses(repository.findActiveBySharedWith(request.requesterId())));
    }

    @Override
    public Result<List<NoteResponse>> findActiveByOwnerAndSharedWith(ListNotesRequest request) {
        if (request == null || request.sharedWith() == null) {
            return Result.failure(ApplicationError.validationError("shared-with account id cannot be null"));
        }

        log.debug("Loading active notes for owner {} shared with {}",
                request.requesterId(), request.sharedWith());
        return Result.success(responses(repository.findActiveByOwnerAndSharedWith(
                request.requesterId(),
                request.sharedWith()
        )));
    }

    @Override
    public Result<List<NoteResponse>> findDeletedByOwner(ListNotesRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note lookup data cannot be null"));
        }

        log.debug("Loading deleted notes for owner {}", request.requesterId());
        return Result.success(responses(repository.findDeletedByOwner(request.requesterId())));
    }

    private List<NoteResponse> responses(List<Note> notes) {
        return notes.stream().map(NoteResponse::from).toList();
    }
}
