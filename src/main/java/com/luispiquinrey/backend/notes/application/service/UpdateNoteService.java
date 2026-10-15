package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.application.dto.UpdateNoteCommand;
import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;

@LogExecution
public class UpdateNoteService {

    private final Logger log;

    private final NoteRepository repository;

    public UpdateNoteService(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    public Note update(String id, UpdateNoteCommand command, String authenticatedAccountId) {
        if (command == null) {
            throw new NoteValidationException("note update data cannot be null");
        }

        log.debug("Updating note {} for account {}", id, authenticatedAccountId);
        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.isOwnedBy(authenticatedAccountId)) {
            throw new NoteAccessDeniedException("you cannot update this note");
        }

        note.rename(command.title());
        note.changeContent(command.content());
        if (command.tags() != null) {
            note.replaceTags(command.tags().stream().map(Tag::new).toList());
        }
        repository.save(note);
        log.debug("Updated note {}", note.id().id());
        return note;
    }
}
