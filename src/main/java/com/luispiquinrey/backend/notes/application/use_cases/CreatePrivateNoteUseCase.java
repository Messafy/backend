package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.CreatePrivateNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.CreatePrivateNotePort;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@LogExecution
@Service
public class CreatePrivateNoteUseCase implements CreatePrivateNotePort {

    private final Logger log;
    private final NoteRepository repository;
    private final NoteIdGenerator idGenerator;
    private final NoteFactory noteFactory = new NoteFactory();

    public CreatePrivateNoteUseCase(NoteRepository repository, NoteIdGenerator idGenerator, Logger log) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.log = log;
    }

    @Override
    public Result<CreatedNoteResponse> create(CreatePrivateNoteRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("note creation data cannot be null"));
        }

        String noteId = idGenerator.nextId();
        log.debug("Creating private note {} for account {}", noteId, request.ownerId());
        Note note;
        try {
            List<Tag> tags = request.tags() == null
                    ? List.of()
                    : request.tags().stream().map(Tag::new).toList();
            note = noteFactory.createPrivateNote(
                    noteId,
                    request.title(),
                    request.content(),
                    request.ownerId(),
                    tags
            );
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(e.getMessage()));
        }

        repository.save(note);
        log.debug("Created private note {}", note.id().id());
        return Result.success(CreatedNoteResponse.from(note));
    }
}
