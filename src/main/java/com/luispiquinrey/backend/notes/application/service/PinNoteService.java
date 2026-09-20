package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.application.dto.PinNoteCommand;
import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.springframework.stereotype.Service;

@Service
public class PinNoteService {

    private final NoteRepository repository;

    public PinNoteService(NoteRepository repository) {
        this.repository = repository;
    }

    public Note setPinned(String id, PinNoteCommand command, String authenticatedAccountId) {
        if (command == null || command.pinned() == null) {
            throw new NoteValidationException("note pin data cannot be null");
        }

        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.isOwnedBy(authenticatedAccountId)) {
            throw new NoteAccessDeniedException("you cannot pin this note");
        }
        if (note.status().isFinal()) {
            throw new NoteConflictException("deleted notes cannot be pinned");
        }

        note.setPinned(command.pinned());
        repository.save(note);
        return note;
    }
}
