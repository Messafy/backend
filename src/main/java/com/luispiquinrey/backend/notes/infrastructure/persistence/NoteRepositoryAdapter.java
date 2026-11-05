package com.luispiquinrey.backend.notes.infrastructure.persistence;

import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.NotePage;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.notes.domain.repository.PageQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NoteRepositoryAdapter implements NoteRepository {

    private static final Logger log = LoggerFactory.getLogger(NoteRepositoryAdapter.class);

    static final String COLLECTION = "notes";
    private static final String TYPE_KEY = "_class";
    private static final String SHARED_TYPE = "shared";
    private static final Sort LISTING_ORDER = Sort.by(
            Sort.Order.desc("pinned"),
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("_id")
    );

    private final NoteMapper mapper;
    private final MongoDataNoteRepository baseRepository;
    private final MongoTemplate mongoTemplate;

    public NoteRepositoryAdapter(
            NoteMapper mapper,
            MongoDataNoteRepository baseRepository,
            MongoTemplate mongoTemplate
    ) {
        this.mapper = mapper;
        this.baseRepository = baseRepository;
        this.mongoTemplate = mongoTemplate;
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
        try {
            NoteDocument saved = baseRepository.save(mapper.toDocument(note));
            note.recordVersion(saved.getVersion());
        } catch (DuplicateKeyException exception) {
            throw new NoteConflictException("note already exists");
        } catch (OptimisticLockingFailureException exception) {
            throw new NoteConflictException("note was modified concurrently; reload and retry");
        }
        log.debug("MongoDB persisted note {}", note.id().id());
    }

    @Override
    public NotePage findAccessibleByStatus(String accountId, NoteStatus status, PageQuery page) {
        return find(new Criteria().andOperator(
                Criteria.where("status").is(status.name()),
                ownedOrReceivedBy(accountId)
        ), page);
    }

    @Override
    public NotePage findActiveByOwner(String ownerId, PageQuery page) {
        return find(new Criteria().andOperator(visible(), ownedOrReceivedBy(ownerId)), page);
    }

    @Override
    public NotePage findActiveBySharedWith(String sharedWith, PageQuery page) {
        return find(new Criteria().andOperator(visible(), receivedBy(sharedWith)), page);
    }

    @Override
    public NotePage findActiveByOwnerAndSharedWith(String ownerId, String sharedWith, PageQuery page) {
        return find(new Criteria().andOperator(
                visible(),
                Criteria.where("ownerId").is(ownerId),
                receivedBy(sharedWith)
        ), page);
    }

    @Override
    public NotePage findDeletedByOwner(String ownerId, PageQuery page) {
        return find(new Criteria().andOperator(
                Criteria.where("status").is(NoteStatus.DELETED.name()),
                Criteria.where("ownerId").is(ownerId)
        ), page);
    }

    private NotePage find(Criteria criteria, PageQuery page) {
        Query query = new Query(criteria).with(LISTING_ORDER);
        if (page.isPaged()) {
            query.skip(page.offset()).limit(page.size());
        }
        log.debug("Querying MongoDB for notes: {}", query.getQueryObject());
        List<Note> notes = mongoTemplate.find(query, NoteDocument.class, COLLECTION).stream()
                .map(mapper::toDomain)
                .toList();
        long total = page.isPaged() && (page.offset() > 0 || notes.size() == page.size())
                ? mongoTemplate.count(new Query(criteria), COLLECTION)
                : page.offset() + notes.size();
        log.debug("MongoDB returned {} of {} notes", notes.size(), total);
        return new NotePage(notes, page, total);
    }

    private static Criteria ownedOrReceivedBy(String accountId) {
        return new Criteria().orOperator(
                Criteria.where("ownerId").is(accountId),
                receivedBy(accountId)
        );
    }

    private static Criteria receivedBy(String accountId) {
        return Criteria.where(TYPE_KEY).is(SHARED_TYPE).and("sharedWith").is(accountId);
    }

    private static Criteria visible() {
        return Criteria.where("status").in(NoteStatus.visibleStatuses().stream().map(Enum::name).toList());
    }
}
