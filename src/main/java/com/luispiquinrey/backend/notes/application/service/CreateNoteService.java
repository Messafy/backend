package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.CreateNoteCommand;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateNoteService {

    private final NoteRepository repository;
    private final AccountLookup accountLookup;
    private final NoteIdGenerator idGenerator;
    private final NoteFactory noteFactory = new NoteFactory();

    public CreateNoteService(NoteRepository repository, AccountLookup accountLookup, NoteIdGenerator idGenerator) {
        this.repository = repository;
        this.accountLookup = accountLookup;
        this.idGenerator = idGenerator;
    }

    public Note createNote(CreateNoteCommand command, String authenticatedAccountId) {
        if (command == null) {
            throw new NoteValidationException("note creation data cannot be null");
        }
        if (command.type() == null) {
            throw new NoteValidationException("note type cannot be null");
        }

        String noteId = idGenerator.nextId();
        List<Tag> tags = command.tags() == null
                ? List.of()
                : command.tags().stream().map(Tag::new).toList();
        Note note = switch (command.type()) {
            case PRIVATE -> noteFactory.createPrivateNote(
                    noteId,
                    command.title(),
                    command.content(),
                    authenticatedAccountId,
                    tags
            );
            case SHARED -> {
                if (command.sharedWith() == null || command.sharedWith().isBlank()) {
                    throw new NoteValidationException("shared note recipient cannot be null or empty");
                }

                if (!accountLookup.existsActiveAccount(command.sharedWith())) {
                    throw new NoteValidationException("shared note recipient must be an active account");
                }

                yield noteFactory.createSharedNote(
                        noteId,
                        command.title(),
                        command.content(),
                        authenticatedAccountId,
                        command.sharedWith(),
                        tags
                );
            }
        };

        repository.save(note);
        return note;
    }
}
