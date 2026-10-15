package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.application.dto.PinNoteCommand;
import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;

@LogExecution
public class PinNoteService {

    private final Logger log;

    private final NoteRepository repository;

    public PinNoteService(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    public Note setPinned(String id, PinNoteCommand command, String authenticatedAccountId) {
        if (command == null || command.pinned() == null) {
            throw new NoteValidationException("note pin data cannot be null");
        }

        log.debug("Setting pinned={} on note {} for account {}", command.pinned(), id, authenticatedAccountId);
        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.isOwnedBy(authenticatedAccountId)) {
            throw new NoteAccessDeniedException("you cannot pin this note");
        }

        note.setPinned(command.pinned());
        repository.save(note);
        log.debug("Set pinned={} on note {}", command.pinned(), note.id().id());
        return note;
    }
}
