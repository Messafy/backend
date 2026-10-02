package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.ShareNoteCommand;
import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.PrivateNote;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.springframework.stereotype.Service;

@Service
public class ShareNoteService {

    private final NoteRepository repository;
    private final AccountLookup accountLookup;
    private final NoteRealtimeNotifier realtimeNotifier;
    private final NoteFactory noteFactory = new NoteFactory();

    public ShareNoteService(
            NoteRepository repository,
            AccountLookup accountLookup,
            NoteRealtimeNotifier realtimeNotifier
    ) {
        this.repository = repository;
        this.accountLookup = accountLookup;
        this.realtimeNotifier = realtimeNotifier;
    }

    public Note share(String id, ShareNoteCommand command, String authenticatedAccountId) {
        if (command == null) {
            throw new NoteValidationException("share command cannot be null");
        }
        if (command.recipientPublicCode() == null || command.recipientPublicCode().isBlank()) {
            throw new NoteValidationException("recipient public code cannot be blank");
        }
        if (command.recipientLockerPassword() == null || command.recipientLockerPassword().isBlank()) {
            throw new NoteValidationException("recipient locker password cannot be blank");
        }

        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.isOwnedBy(authenticatedAccountId)) {
            throw new NoteAccessDeniedException("you cannot share this note");
        }

        if (!(note instanceof PrivateNote privateNote)) {
            throw new NoteConflictException("only private notes can be shared");
        }

        UserId recipientId = accountLookup.findActiveAccountIdByPublicCode(command.recipientPublicCode())
                .orElseThrow(() -> new NoteValidationException(
                        "no active account was found for the provided public code"
                ));

        if (!accountLookup.matchesLockerPassword(recipientId, command.recipientLockerPassword())) {
            throw new NoteValidationException("locker password does not match the recipient");
        }

        Note sharedNote = noteFactory.restoreSharedNote(
                privateNote.id().id(),
                privateNote.title().title(),
                privateNote.content().content(),
                privateNote.status(),
                privateNote.createdAt(),
                privateNote.readAt(),
                privateNote.hiddenAt(),
                privateNote.reportedAt(),
                authenticatedAccountId,
                recipientId.id(),
                privateNote.tags(),
                privateNote.pinned()
        );

        repository.delete(privateNote);
        repository.save(sharedNote);
        realtimeNotifier.notifySharedNote(sharedNote, recipientId.id());

        return sharedNote;
    }
}
