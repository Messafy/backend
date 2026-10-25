package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.request.ShareNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.NoteResponse;
import com.luispiquinrey.backend.notes.application.port.ShareNotePort;
import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.PrivateNote;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Service;

import java.util.Optional;

@LogExecution
@Service
public class ShareNoteUseCase implements ShareNotePort {

    private final Logger log;
    private final NoteRepository repository;
    private final AccountLookup accountLookup;
    private final NoteRealtimeNotifier realtimeNotifier;
    private final NoteFactory noteFactory = new NoteFactory();

    public ShareNoteUseCase(
            NoteRepository repository,
            AccountLookup accountLookup,
            NoteRealtimeNotifier realtimeNotifier,
            Logger log
    ) {
        this.repository = repository;
        this.accountLookup = accountLookup;
        this.realtimeNotifier = realtimeNotifier;
        this.log = log;
    }

    @Override
    public Result<NoteResponse> share(ShareNoteRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("share command cannot be null"));
        }

        log.debug("Sharing note {} for account {}", request.noteId(), request.requesterId());
        var found = repository.findById(new NoteId(request.noteId()));
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFoundError("note not found: " + request.noteId()));
        }
        Note note = found.get();

        if (!note.isOwnedBy(request.requesterId())) {
            return Result.failure(ApplicationError.forbiddenError("you cannot share this note"));
        }
        if (!(note instanceof PrivateNote privateNote)) {
            return Result.failure(ApplicationError.conflictError("only private notes can be shared"));
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

        Note sharedNote;
        try {
            sharedNote = noteFactory.restoreSharedNote(
                    privateNote.id().id(),
                    privateNote.title().title(),
                    privateNote.content().content(),
                    privateNote.status(),
                    privateNote.createdAt(),
                    privateNote.readAt(),
                    privateNote.hiddenAt(),
                    privateNote.reportedAt(),
                    request.requesterId(),
                    recipientId.get().id(),
                    privateNote.tags(),
                    privateNote.pinned()
            );
        } catch (NoteConflictException e) {
            return Result.failure(ApplicationError.conflictError(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(e.getMessage()));
        }

        repository.save(sharedNote);
        log.debug("Shared note {} with account {}", sharedNote.id().id(), recipientId.get().id());
        realtimeNotifier.notifySharedNote(sharedNote, recipientId.get().id());
        return Result.success(NoteResponse.from(sharedNote));
    }
}
