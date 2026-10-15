package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;

@LogExecution
public class MarkNoteAsReadService {

    private final Logger log;

    private final NoteRepository repository;

    public MarkNoteAsReadService(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    public Note markAsRead(String id, String accountId) {
        log.debug("Marking note {} as read for account {}", id, accountId);
        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.canAccess(accountId)) {
            throw new NoteAccessDeniedException("only the owner or the shared recipient can mark a note as read");
        }

        note.markAsRead();
        repository.save(note);
        log.debug("Marked note {} as read", note.id().id());
        return note;
    }

}
