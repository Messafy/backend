package com.luispiquinrey.backend.notes.infrastructure.persistence;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.time.Date;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class NoteRepositoryAdapter implements NoteRepository {

    private static final Logger log = LoggerFactory.getLogger(NoteRepositoryAdapter.class);

    private final NoteMapper mapper;
    private final MongoDataNoteRepository baseRepository;
    private final MongoDataPrivateNoteRepository privateRepository;
    private final MongoDataSharedNoteRepository sharedRepository;

    public NoteRepositoryAdapter(
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
    public void save(Note note) {
        log.debug("Persisting note {} in MongoDB", note.id().id());
        baseRepository.findById(mapper.toObjectId(note.id().id()))
                .ifPresentOrElse(
                        document -> updateExistingDocument(document, note),
                        () -> baseRepository.save(mapper.toDocument(note))
                );
        log.debug("MongoDB persisted note {}", note.id().id());
    }

    @Override
    public void delete(Note note) {
        log.debug("Deleting note {} from MongoDB", note.id().id());
        baseRepository.deleteById(mapper.toObjectId(note.id().id()));
        log.debug("MongoDB deleted note {}", note.id().id());
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
        List<String> visibleStatuses = List.of(NoteStatus.NEW.name(), NoteStatus.READ.name());
        List<NoteDocument> documents = new ArrayList<>();
        documents.addAll(privateRepository.findAllByOwnerIdAndStatusIn(ownerId, visibleStatuses));
        documents.addAll(sharedRepository.findAllByOwnerIdAndStatusIn(ownerId, visibleStatuses));
        documents.addAll(sharedRepository.findAllByStatusInAndSharedWith(visibleStatuses, ownerId));
        List<Note> notes = dedupeById(documents).stream().map(mapper::toDomain).toList();
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

    @Override
    public List<Note> findDeletedByOwner(String ownerId) {
        List<NoteDocument> documents = new ArrayList<>();
        documents.addAll(privateRepository.findAllByOwnerIdAndStatus(ownerId, NoteStatus.DELETED.name()));
        documents.addAll(sharedRepository.findAllByOwnerIdAndStatus(ownerId, NoteStatus.DELETED.name()));
        return toDomainList(documents);
    }

    private List<NoteDocument> dedupeById(List<NoteDocument> documents) {
        Map<ObjectId, NoteDocument> unique = new LinkedHashMap<>();
        documents.forEach(document -> unique.putIfAbsent(document.getId(), document));
        return new ArrayList<>(unique.values());
    }

    private List<Note> toDomainList(List<NoteDocument> documents) {
        return documents.stream().map(mapper::toDomain).toList();
    }

    private void updateExistingDocument(NoteDocument document, Note note) {
        document.setTitle(note.title().title());
        document.setContent(note.content().content());
        document.setStatus(note.status().name());
        document.setReadAt(toIsoString(note.readAt()));
        document.setHiddenAt(toIsoString(note.hiddenAt()));
        document.setReportedAt(toIsoString(note.reportedAt()));
        document.setTags(note.tags().stream().map(Tag::tag).toList());
        document.setPinned(note.pinned());
        baseRepository.save(document);
    }

    private String toIsoString(Date date) {
        return date == null ? null : date.value().toString();
    }
}
