package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.infrastructure.MongoDataNoteRepository;
import com.luispiquinrey.backend.notes.infrastructure.NoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class MongoCreateNoteRepository implements CreateNoteRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoCreateNoteRepository.class);

    private final NoteMapper noteMapper;
    private final MongoDataNoteRepository mongoDataNoteRepository;

    public MongoCreateNoteRepository(NoteMapper noteMapper, MongoDataNoteRepository mongoDataNoteRepository) {
        this.noteMapper = noteMapper;
        this.mongoDataNoteRepository = mongoDataNoteRepository;
    }

    @Override
    public void save(Note note) {
        log.debug("Persisting new note {} in MongoDB", note.id().id());
        mongoDataNoteRepository.save(noteMapper.toDocument(note));
        log.debug("MongoDB persisted new note {}", note.id().id());
    }
}
