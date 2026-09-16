package com.luispiquinrey.backend.notes.slices.update;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.Tag;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class UpdateNoteService {

    private final UpdateNoteRepository repository;

    public UpdateNoteService(UpdateNoteRepository repository) {
        this.repository = repository;
    }

    public Note update(String id, NoteUpdateRequest request, String authenticatedAccountId) {
        if (request == null) {
            throw new NoteValidationException("note update data cannot be null");
        }

        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.isOwnedBy(authenticatedAccountId)) {
            throw new AccessDeniedException("you cannot update this note");
        }

        note.rename(request.title());
        note.changeContent(request.content());
        if (request.tags() != null) {
            note.replaceTags(request.tags().stream().map(Tag::new).toList());
        }
        repository.save(note);
        return note;
    }
}
