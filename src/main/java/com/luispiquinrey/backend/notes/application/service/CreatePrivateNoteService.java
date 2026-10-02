package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.application.dto.CreatePrivateNoteCommand;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreatePrivateNoteService {

    private final NoteRepository repository;
    private final NoteIdGenerator idGenerator;
    private final NoteFactory noteFactory = new NoteFactory();

    public CreatePrivateNoteService(NoteRepository repository, NoteIdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public Note create(CreatePrivateNoteCommand command, String authenticatedAccountId) {
        if (command == null) {
            throw new NoteValidationException("note creation data cannot be null");
        }
        if (authenticatedAccountId == null || authenticatedAccountId.isBlank()) {
            throw new NoteValidationException("authenticated account id cannot be blank");
        }

        String noteId = idGenerator.nextId();
        List<Tag> tags = command.tags() == null
                ? List.of()
                : command.tags().stream().map(Tag::new).toList();

        Note note = noteFactory.createPrivateNote(
                noteId,
                command.title(),
                command.content(),
                authenticatedAccountId,
                tags
        );
        repository.save(note);
        return note;
    }
}