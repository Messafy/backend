package com.luispiquinrey.backend.notes.slices.delete;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.notes.infrastructure.MongoDataNoteRepository;
import com.luispiquinrey.backend.notes.infrastructure.NoteDocument;
import com.luispiquinrey.backend.notes.infrastructure.NoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoDeleteNoteRepository implements DeleteNoteRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoDeleteNoteRepository.class);

    private final NoteMapper mapper;
    private final MongoDataNoteRepository repository;

    public MongoDeleteNoteRepository(NoteMapper mapper, MongoDataNoteRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Optional<Note> findById(NoteId id) {
        log.debug("Querying MongoDB for note {} before deletion", id.id());
        Optional<Note> note = repository.findById(mapper.toObjectId(id.id()))
                .map(mapper::toDomain);
        log.debug("MongoDB {} note {} before deletion", note.isPresent() ? "found" : "did not find", id.id());
        return note;
    }

    @Override
    public void save(Note note) {
        log.debug("Updating note {} deletion status in MongoDB", note.id().id());
        NoteDocument noteDocument = repository.findById(mapper.toObjectId(note.id().id()))
                .orElseThrow(() -> {
                    log.warn("Cannot update note {} deletion status because MongoDB document was not found", note.id().id());
                    return new NoteNotFoundException(note.id().id());
                });

        noteDocument.setStatus(note.status().name());
        repository.save(noteDocument);
        log.debug("MongoDB updated note {} deletion status to {}", note.id().id(), note.status());
    }
}
