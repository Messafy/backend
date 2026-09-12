package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteValidationException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateNoteService {

    private static final Logger log = LoggerFactory.getLogger(CreateNoteService.class);

    private final CreateNoteRepository repository;
    private final NoteFactory noteFactory = new NoteFactory();

    public CreateNoteService(CreateNoteRepository repository) {
        this.repository = repository;
    }

    public Note createNote(NoteCreationRequest noteCreationRequest) {
        if (noteCreationRequest == null) {
            log.warn("Note creation rejected because request body is missing");
            throw new NoteValidationException("note creation data cannot be null");
        }
        if (noteCreationRequest.type() == null) {
            log.warn("Note creation rejected because note type is missing for owner {}", noteCreationRequest.ownerId());
            throw new NoteValidationException("note type cannot be null");
        }

        String noteId = new ObjectId().toHexString();
        log.info("Creating {} note {} for owner {}", noteCreationRequest.type(), noteId, noteCreationRequest.ownerId());

        Note note = switch (noteCreationRequest.type()) {
            case PRIVATE -> noteFactory.createPrivateNote(
                    noteId,
                    noteCreationRequest.title(),
                    noteCreationRequest.content(),
                    noteCreationRequest.ownerId()
            );
            case SHARED -> noteFactory.createSharedNote(
                    noteId,
                    noteCreationRequest.title(),
                    noteCreationRequest.content(),
                    noteCreationRequest.ownerId(),
                    noteCreationRequest.sharedWith()
            );
        };

        repository.save(note);
        log.info("Note {} stored successfully with status {}", note.id().id(), note.status());
        return note;
    }
}
