package com.luispiquinrey.backend.notes.slices.get;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import com.luispiquinrey.backend.notes.infrastructure.MongoDataNoteRepository;
import com.luispiquinrey.backend.notes.infrastructure.MongoDataPrivateNoteRepository;
import com.luispiquinrey.backend.notes.infrastructure.MongoDataSharedNoteRepository;
import com.luispiquinrey.backend.notes.infrastructure.NoteDocument;
import com.luispiquinrey.backend.notes.infrastructure.NoteMapper;
import com.luispiquinrey.backend.notes.infrastructure.PrivateNoteDocument;
import com.luispiquinrey.backend.notes.infrastructure.SharedNoteDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MongoGetNoteRepository implements GetNoteRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoGetNoteRepository.class);

    private final NoteMapper mapper;
    private final MongoDataNoteRepository baseRepository;
    private final MongoDataPrivateNoteRepository privateRepository;
    private final MongoDataSharedNoteRepository sharedRepository;

    public MongoGetNoteRepository(
            NoteMapper mapper,
            MongoDataNoteRepository baseRepository,
            MongoDataPrivateNoteRepository privateRepository,
            MongoDataSharedNoteRepository sharedRepository
    ) {
        this.mapper = mapper;
        this.baseRepository = baseRepository;
        this.privateRepository = privateRepository;
        this.sharedRepository = sharedRepository;
    }

    @Override
    public Optional<Note> findById(NoteId id) {
        log.debug("Querying MongoDB for note {}", id.id());
        Optional<Note> note = baseRepository.findById(mapper.toObjectId(id.id()))
                .map(mapper::toDomain);
        log.debug("MongoDB {} note {}", note.isPresent() ? "found" : "did not find", id.id());
        return note;
    }

    @Override
    public List<Note> findByStatus(String status) {
        log.debug("Querying MongoDB for notes with status {}", status);
        List<Note> notes = baseRepository.findAllByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .toList();
        log.debug("MongoDB returned {} notes with status {}", notes.size(), status);
        return notes;
    }

    @Override
    public List<Note> findActiveByOwner(String ownerId) {
        log.debug("Querying MongoDB for active notes owned by or shared with {}", ownerId);
        List<NoteDocument> documents = new ArrayList<>();
        documents.addAll(privateRepository.findAllByOwnerIdAndStatus(ownerId, NoteStatus.NEW.name()));
        documents.addAll(sharedRepository.findAllByStatusAndSharedWith(
                NoteStatus.NEW.name(),
                ownerId
        ));
        List<Note> notes = documents.stream().map(mapper::toDomain).toList();
        log.debug("MongoDB returned {} active notes for owner {}", notes.size(), ownerId);
        return notes;
    }

    @Override
    public List<Note> findActiveBySharedWith(String sharedWith) {
        log.debug("Querying MongoDB for active notes shared with {}", sharedWith);
        List<Note> notes = sharedRepository.findAllByStatusAndSharedWith(NoteStatus.NEW.name(), sharedWith)
                .stream()
                .map(mapper::toDomain)
                .toList();
        log.debug("MongoDB returned {} active notes shared with {}", notes.size(), sharedWith);
        return notes;
    }

    @Override
    public List<Note> findActiveByOwnerAndSharedWith(String ownerId, String sharedWith) {
        log.debug("Querying MongoDB for active notes owned by {} and shared with {}", ownerId, sharedWith);
        List<Note> notes = sharedRepository.findAllByStatusAndOwnerIdAndSharedWith(
                NoteStatus.NEW.name(),
                ownerId,
                sharedWith
        ).stream().map(mapper::toDomain).toList();
        log.debug("MongoDB returned {} active notes owned by {} and shared with {}", notes.size(), ownerId, sharedWith);
        return notes;
    }

    List<PrivateNoteDocument> findPrivateByOwner(String ownerId) {
        return privateRepository.findAllByOwnerIdAndStatus(ownerId, NoteStatus.NEW.name());
    }

    List<SharedNoteDocument> findSharedForOwner(String ownerId) {
        return sharedRepository.findAllByStatusAndSharedWith(NoteStatus.NEW.name(), ownerId);
    }
}
