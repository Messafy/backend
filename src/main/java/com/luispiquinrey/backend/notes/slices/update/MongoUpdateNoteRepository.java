package com.luispiquinrey.backend.notes.slices.update;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.notes.infrastructure.MongoDataNoteRepository;
import com.luispiquinrey.backend.notes.infrastructure.NoteDocument;
import com.luispiquinrey.backend.notes.infrastructure.NoteMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoUpdateNoteRepository implements UpdateNoteRepository {

    private final NoteMapper mapper;
    private final MongoDataNoteRepository repository;

    public MongoUpdateNoteRepository(NoteMapper mapper, MongoDataNoteRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Optional<Note> findById(NoteId id) {
        return repository.findById(mapper.toObjectId(id.id()))
                .map(mapper::toDomain);
    }

    @Override
    public void save(Note note) {
        NoteDocument document = repository.findById(mapper.toObjectId(note.id().id()))
                .orElseThrow(() -> new NoteNotFoundException(note.id().id()));

        document.setTitle(note.title().title());
        document.setContent(note.content().content());
        repository.save(document);
    }
}
