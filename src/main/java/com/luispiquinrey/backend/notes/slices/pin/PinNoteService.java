package com.luispiquinrey.backend.notes.slices.pin;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.NoteValidationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PinNoteService {
    private final PinNoteRepository repository;

    public PinNoteService(PinNoteRepository repository) {
        this.repository = repository;
    }

    public Note setPinned(String id, NotePinRequest request, String authenticatedAccountId) {
        if (request == null || request.pinned() == null) {
            throw new NoteValidationException("note pin data cannot be null");
        }

        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.isOwnedBy(authenticatedAccountId)) {
            throw new AccessDeniedException("you cannot pin this note");
        }
        if (note.status().isFinal()) {
            throw new NoteConflictException("deleted notes cannot be pinned");
        }

        note.setPinned(request.pinned());
        repository.save(note);
        return note;
    }
}
