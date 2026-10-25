package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.request.CreateSharedNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.CreateSharedNotePort;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@LogExecution
@Service
public class CreateSharedNoteUseCase implements CreateSharedNotePort {

    private final Logger log;
    private final NoteRepository repository;
    private final AccountLookup accountLookup;
    private final NoteIdGenerator idGenerator;
    private final NoteRealtimeNotifier realtimeNotifier;
    private final NoteFactory noteFactory = new NoteFactory();

    public CreateSharedNoteUseCase(
            NoteRepository repository,
            AccountLookup accountLookup,
            NoteIdGenerator idGenerator,
            NoteRealtimeNotifier realtimeNotifier,
            Logger log
    ) {
        this.repository = repository;
        this.accountLookup = accountLookup;
        this.idGenerator = idGenerator;
        this.realtimeNotifier = realtimeNotifier;
        this.log = log;
    }

    @Override
    public Result<CreatedNoteResponse> create(CreateSharedNoteRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note creation data cannot be null"));
        }

        Optional<UserId> recipientId = accountLookup.findActiveAccountIdByPublicCode(request.recipientPublicCode());
        if (recipientId.isEmpty()) {
            return Result.failure(ApplicationError.validationError(
                    "no active account was found for the provided public code"
            ));
        }
        if (!accountLookup.matchesLockerPassword(recipientId.get(), request.recipientLockerPassword())) {
            return Result.failure(ApplicationError.validationError(
                    "locker password does not match the recipient"
            ));
        }

        String noteId = idGenerator.nextId();
        log.debug("Creating shared note {} for account {}", noteId, request.ownerId());
        Note note;
        try {
            List<Tag> tags = request.tags() == null
                    ? List.of()
                    : request.tags().stream().map(Tag::new).toList();
            note = noteFactory.createSharedNote(
                    noteId,
                    request.title(),
                    request.content(),
                    request.ownerId(),
                    recipientId.get().id(),
                    tags
            );
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(e.getMessage()));
        }

        repository.save(note);
        log.debug("Created shared note {} for recipient {}", note.id().id(), recipientId.get().id());
        realtimeNotifier.notifySharedNote(note, recipientId.get().id());
        return Result.success(CreatedNoteResponse.from(note));
    }
}
