package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.Tag;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateNoteService {

    private static final Logger log = LoggerFactory.getLogger(CreateNoteService.class);

    private final CreateNoteRepository repository;
    private final AccountLookup accountLookup;
    private final NoteFactory noteFactory = new NoteFactory();

    public CreateNoteService(CreateNoteRepository repository, AccountLookup accountLookup) {
        this.repository = repository;
        this.accountLookup = accountLookup;
    }

    public Note createNote(NoteCreationRequest noteCreationRequest, String authenticatedAccountId) {
        if (noteCreationRequest == null) {
            log.warn("Note creation rejected because request body is missing");
            throw new NoteValidationException("note creation data cannot be null");
        }
        if (noteCreationRequest.type() == null) {
            log.warn("Note creation rejected because note type is missing for owner {}", authenticatedAccountId);
            throw new NoteValidationException("note type cannot be null");
        }

        String noteId = new ObjectId().toHexString();
        var tags = noteCreationRequest.tags() == null
                ? java.util.List.<Tag>of()
                : noteCreationRequest.tags().stream().map(Tag::new).toList();
        log.info("Creating {} note {} for owner {}", noteCreationRequest.type(), noteId, authenticatedAccountId);

        Note note = switch (noteCreationRequest.type()) {
            case PRIVATE -> noteFactory.createPrivateNote(
                    noteId,
                    noteCreationRequest.title(),
                    noteCreationRequest.content(),
                    authenticatedAccountId,
                    tags
            );
            case SHARED -> {
                if (noteCreationRequest.sharedWith() == null || noteCreationRequest.sharedWith().isBlank()) {
                    log.warn("Shared note {} rejected because recipient is missing", noteId);
                    throw new NoteValidationException("shared note recipient cannot be null or empty");
                }

                if (!accountLookup.existsActiveAccount(noteCreationRequest.sharedWith())) {
                    log.warn("Shared note {} rejected because recipient {} is not an active account",
                            noteId, noteCreationRequest.sharedWith());
                    throw new NoteValidationException("shared note recipient must be an active account");
                }

                yield noteFactory.createSharedNote(
                        noteId,
                        noteCreationRequest.title(),
                        noteCreationRequest.content(),
                        authenticatedAccountId,
                        noteCreationRequest.sharedWith(),
                        tags
                );
            }
        };

        repository.save(note);
        log.info("Note {} stored successfully with status {}", note.id().id(), note.status());
        return note;
    }
}
