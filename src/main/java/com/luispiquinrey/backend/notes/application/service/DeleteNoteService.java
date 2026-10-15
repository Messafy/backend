package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;

@LogExecution
public class DeleteNoteService {

    private final Logger log;

    private final NoteRepository repository;

    public DeleteNoteService(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    public void delete(String id, String authenticatedAccountId) {
        log.debug("Deleting note {} for account {}", id, authenticatedAccountId);
        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.isOwnedBy(authenticatedAccountId)) {
            throw new NoteAccessDeniedException("you cannot delete this note");
        }

        note.delete();
        repository.save(note);
        log.debug("Deleted note {}", note.id().id());
    }
}
