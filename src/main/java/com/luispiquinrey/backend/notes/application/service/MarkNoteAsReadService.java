package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.springframework.stereotype.Service;

@Service
public class MarkNoteAsReadService {

    private final NoteRepository repository;

    public MarkNoteAsReadService(NoteRepository repository) {
        this.repository = repository;
    }

    public Note markAsRead(String id, String accountId) {
        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!canAccess(note, accountId)) {
            throw new NoteAccessDeniedException("only the owner or the shared recipient can mark a note as read");
        }

        note.markAsRead();
        repository.save(note);
        return note;
    }

    private boolean canAccess(Note note, String accountId) {
        return note.isOwnedBy(accountId)
                || note instanceof SharedNote sharedNote && sharedNote.isSharedWith(accountId);
    }
}
