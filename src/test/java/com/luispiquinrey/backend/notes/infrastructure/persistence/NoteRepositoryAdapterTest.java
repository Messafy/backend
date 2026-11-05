package com.luispiquinrey.backend.notes.infrastructure.persistence;

import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.NotePage;
import com.luispiquinrey.backend.notes.domain.repository.PageQuery;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteRepositoryAdapterTest {

    private static final String NOTE_ID = "507f1f77bcf86cd799439011";
    private static final String OWNER_ID = "507f1f77bcf86cd799439012";
    private static final String RECIPIENT_ID = "507f1f77bcf86cd799439013";

    @Mock
    private MongoDataNoteRepository baseRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private final NoteMapper mapper = new NoteMapper();
    private NoteRepositoryAdapter repository;

    @BeforeEach
    void setUp() {
        repository = new NoteRepositoryAdapter(mapper, baseRepository, mongoTemplate);
    }

    @Test
    @Timeout(1)
    @Tag("mongoSaveNoteRepository")
    void savesNewNoteWithoutVersionAndRecordsTheStoredVersion() {
        Note note = privateNote();
        when(baseRepository.save(any(NoteDocument.class))).thenAnswer(invocation -> {
            NoteDocument document = invocation.getArgument(0);
            assertNull(document.getVersion());
            document.setVersion(0);
            return document;
        });

        repository.save(note);

        assertEquals(0, note.version());
    }

    @Test
    @Timeout(1)
    @Tag("mongoSaveNoteRepository")
    void savesTheVersionTheNoteWasLoadedWithInsteadOfReloadingIt() {
        Note note = privateNote();
        note.recordVersion(3);
        note.markAsRead();
        ArgumentCaptor<NoteDocument> saved = ArgumentCaptor.forClass(NoteDocument.class);
        when(baseRepository.save(saved.capture())).thenAnswer(invocation -> {
            NoteDocument document = invocation.getArgument(0);
            document.setVersion(4);
            return document;
        });

        repository.save(note);

        assertEquals("READ", saved.getValue().getStatus());
        assertEquals(note.readAt().value().toString(), saved.getValue().getReadAt());
        assertEquals(4, note.version());
        verify(baseRepository, never()).findById(any());
    }

    @Test
    @Timeout(1)
    @Tag("mongoSaveNoteRepository")
    void translatesConcurrentModificationIntoNoteConflict() {
        Note note = privateNote();
        note.recordVersion(1);
        when(baseRepository.save(any(NoteDocument.class)))
                .thenThrow(new OptimisticLockingFailureException("stale"));

        assertThrows(NoteConflictException.class, () -> repository.save(note));
        assertEquals(1, note.version());
    }

    @Test
    @Timeout(1)
    @Tag("mongoSaveNoteRepository")
    void translatesDuplicateIdIntoNoteConflict() {
        when(baseRepository.save(any(NoteDocument.class))).thenThrow(new DuplicateKeyException("duplicate"));

        assertThrows(NoteConflictException.class, () -> repository.save(privateNote()));
    }

    @Test
    @Timeout(1)
    @Tag("mongoGetNoteRepository")
    void findsNoteByIdWithItsVersion() {
        PrivateNoteDocument document = (PrivateNoteDocument) mapper.toDocument(privateNote());
        document.setVersion(2);
        when(baseRepository.findById(new ObjectId(NOTE_ID))).thenReturn(Optional.of(document));

        Note found = repository.findById(new NoteId(NOTE_ID)).orElseThrow();

        assertEquals(NOTE_ID, found.id().id());
        assertEquals(2, found.version());
    }

    @Test
    @Timeout(1)
    @Tag("mongoGetNoteRepository")
    void returnsEmptyWhenTheNoteDoesNotExist() {
        when(baseRepository.findById(new ObjectId(NOTE_ID))).thenReturn(Optional.empty());

        assertTrue(repository.findById(new NoteId(NOTE_ID)).isEmpty());
    }

    @Test
    @Timeout(1)
    @Tag("mongoListNoteRepository")
    void filtersByStatusAndRequesterInTheDatabase() {
        Query query = captureListing(() -> repository.findAccessibleByStatus(OWNER_ID, NoteStatus.READ, PageQuery.unpaged()));

        Document filter = query.getQueryObject();
        String json = filter.toJson();
        assertTrue(json.contains("\"status\": \"READ\""), json);
        assertTrue(json.contains("\"ownerId\": \"" + OWNER_ID + "\""), json);
        assertTrue(json.contains("\"sharedWith\": \"" + OWNER_ID + "\""), json);
        assertEquals(0, query.getSkip());
        assertEquals(0, query.getLimit());
    }

    @Test
    @Timeout(1)
    @Tag("mongoListNoteRepository")
    void restrictsReceivedNotesToSharedDocuments() {
        Query query = captureListing(() -> repository.findActiveBySharedWith(RECIPIENT_ID, PageQuery.unpaged()));

        String json = query.getQueryObject().toJson();
        assertTrue(json.contains("\"_class\": \"shared\""), json);
        assertTrue(json.contains("\"sharedWith\": \"" + RECIPIENT_ID + "\""), json);
        assertTrue(json.contains("\"NEW\"") && json.contains("\"READ\""), json);
    }

    @Test
    @Timeout(1)
    @Tag("mongoListNoteRepository")
    void sortsPinnedFirstThenNewestFirst() {
        Query query = captureListing(() -> repository.findActiveByOwner(OWNER_ID, PageQuery.unpaged()));

        assertEquals(new Document("pinned", -1).append("createdAt", -1).append("_id", -1), query.getSortObject());
    }

    @Test
    @Timeout(1)
    @Tag("mongoListNoteRepository")
    void appliesSkipAndLimitAndCountsTheTotalForPagedQueries() {
        PrivateNoteDocument document = (PrivateNoteDocument) mapper.toDocument(privateNote());
        when(mongoTemplate.find(any(Query.class), eq(NoteDocument.class), eq("notes")))
                .thenReturn(List.of(document, document));
        when(mongoTemplate.count(any(Query.class), eq("notes"))).thenReturn(7L);

        NotePage page = repository.findDeletedByOwner(OWNER_ID, new PageQuery(1, 2));

        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(query.capture(), eq(NoteDocument.class), eq("notes"));
        assertEquals(2, query.getValue().getSkip());
        assertEquals(2, query.getValue().getLimit());
        assertEquals(2, page.items().size());
        assertEquals(7, page.totalItems());
    }

    @Test
    @Timeout(1)
    @Tag("mongoListNoteRepository")
    void skipsTheCountWhenTheFirstPageIsNotFull() {
        PrivateNoteDocument document = (PrivateNoteDocument) mapper.toDocument(privateNote());
        when(mongoTemplate.find(any(Query.class), eq(NoteDocument.class), eq("notes")))
                .thenReturn(List.of(document));

        NotePage page = repository.findActiveByOwner(OWNER_ID, new PageQuery(0, 20));

        assertEquals(1, page.totalItems());
        verify(mongoTemplate, never()).count(any(Query.class), eq("notes"));
    }

    private Query captureListing(Runnable listing) {
        when(mongoTemplate.find(any(Query.class), eq(NoteDocument.class), eq("notes"))).thenReturn(List.of());
        listing.run();
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(query.capture(), eq(NoteDocument.class), eq("notes"));
        return query.getValue();
    }

    private Note privateNote() {
        return new NoteFactory().createPrivateNote(NOTE_ID, "Hello", "Body", OWNER_ID);
    }
}
