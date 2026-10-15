package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.CreateSharedNoteCommand;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;

import java.util.List;

@LogExecution
public class CreateSharedNoteService {

    private final Logger log;

    private final NoteRepository repository;
    private final AccountLookup accountLookup;
    private final NoteIdGenerator idGenerator;
    private final NoteRealtimeNotifier realtimeNotifier;
    private final NoteFactory noteFactory = new NoteFactory();

    public CreateSharedNoteService(
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

    public Note create(CreateSharedNoteCommand command, String authenticatedAccountId) {
        if (command == null) {
            throw new NoteValidationException("note creation data cannot be null");
        }
        if (authenticatedAccountId == null || authenticatedAccountId.isBlank()) {
            throw new NoteValidationException("authenticated account id cannot be blank");
        }
        if (command.recipientPublicCode() == null || command.recipientPublicCode().isBlank()) {
            throw new NoteValidationException("recipient public code cannot be blank");
        }
        if (command.recipientLockerPassword() == null || command.recipientLockerPassword().isBlank()) {
            throw new NoteValidationException("recipient locker password cannot be blank");
        }

        UserId recipientId = accountLookup.findActiveAccountIdByPublicCode(command.recipientPublicCode())
                .orElseThrow(() -> new NoteValidationException(
                        "no active account was found for the provided public code"
                ));

        if (!accountLookup.matchesLockerPassword(recipientId, command.recipientLockerPassword())) {
            throw new NoteValidationException("locker password does not match the recipient");
        }

        String noteId = idGenerator.nextId();
        log.debug("Creating shared note {} for account {}", noteId, authenticatedAccountId);
        List<Tag> tags = command.tags() == null
                ? List.of()
                : command.tags().stream().map(Tag::new).toList();

        Note note = noteFactory.createSharedNote(
                noteId,
                command.title(),
                command.content(),
                authenticatedAccountId,
                recipientId.id(),
                tags
        );
        repository.save(note);
        log.debug("Created shared note {} for recipient {}", note.id().id(), recipientId.id());
        realtimeNotifier.notifySharedNote(note, recipientId.id());
        return note;
    }
}
