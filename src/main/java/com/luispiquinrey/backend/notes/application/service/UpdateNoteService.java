package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.application.dto.UpdateNoteCommand;
import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateNoteService {

    private final NoteRepository repository;

    public UpdateNoteService(NoteRepository repository) {
        this.repository = repository;
    }

    public Note update(String id, UpdateNoteCommand command, String authenticatedAccountId) {
        if (command == null) {
            throw new NoteValidationException("note update data cannot be null");
        }

        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> new NoteNotFoundException(id));

        if (!note.isOwnedBy(authenticatedAccountId)) {
            throw new NoteAccessDeniedException("you cannot update this note");
        }

        if (note instanceof SharedNote) {
            throw new NoteConflictException("shared notes cannot be edited after they are sent");
        }

        note.rename(command.title());
        note.changeContent(command.content());
        if (command.tags() != null) {
            note.replaceTags(command.tags().stream().map(Tag::new).toList());
        }
        repository.save(note);
        return note;
    }
}
