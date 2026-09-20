package com.luispiquinrey.backend.notes.infrastructure.persistence;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteRepositoryAdapterTest {

    @Mock
    private NoteMapper mapper;

    @Mock
    private MongoDataNoteRepository baseRepository;

    @Mock
    private MongoDataPrivateNoteRepository privateRepository;

    @Mock
    private MongoDataSharedNoteRepository sharedRepository;

    @InjectMocks
    private NoteRepositoryAdapter repository;

    @Test
    @Timeout(1)
    @Tag("mongoCreateNoteRepository")
    void shouldMapAndPersistNote() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        PrivateNoteDocument document = new PrivateNoteDocument(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "NEW",
                note.createdAt().value().toString(),
                null,
                null,
                null,
                "507f1f77bcf86cd799439012"
        );

        when(mapper.toDocument(note)).thenReturn(document);

        repository.save(note);

        verify(mapper).toDocument(note);
        verify(baseRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoCreateNoteRepository")
    void shouldMapAndPersistSharedNote() {
        Note note = new NoteFactory().createSharedNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
        SharedNoteDocument document = new SharedNoteDocument(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "NEW",
                note.createdAt().value().toString(),
                null,
                null,
                null,
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );

        when(mapper.toDocument(note)).thenReturn(document);

        repository.save(note);

        verify(mapper).toDocument(note);
        verify(baseRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoGetNoteRepository")
    void shouldReturnNoteWhenDocumentExists() {
        PrivateNoteDocument document = privateDocument();
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");
        Note expectedNote = privateNote("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(expectedNote);

        Optional<Note> result = repository.findById(new NoteId("507f1f77bcf86cd799439011"));

        assertTrue(result.isPresent());
        assertEquals(expectedNote, result.get());
        verify(mapper).toObjectId("507f1f77bcf86cd799439011");
        verify(baseRepository).findById(objectId);
        verify(mapper).toDomain(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoGetNoteRepository")
    void shouldReturnEmptyWhenDocumentDoesNotExist() {
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.empty());

        Optional<Note> result = repository.findById(new NoteId("507f1f77bcf86cd799439011"));

        assertTrue(result.isEmpty());
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @Timeout(1)
    @Tag("mongoGetNoteRepository")
    void shouldFindAllNotesByStatus() {
        PrivateNoteDocument firstDocument = privateDocument();
        SharedNoteDocument secondDocument = sharedDocument();
        Note firstNote = privateNote("507f1f77bcf86cd799439011");
        Note secondNote = sharedNote();

        when(baseRepository.findAllByStatus("READ")).thenReturn(List.of(firstDocument, secondDocument));
        when(mapper.toDomain(firstDocument)).thenReturn(firstNote);
        when(mapper.toDomain(secondDocument)).thenReturn(secondNote);

        List<Note> result = repository.findByStatus("READ");

        assertEquals(List.of(firstNote, secondNote), result);
        verify(baseRepository).findAllByStatus("READ");
    }

    @Test
    @Timeout(1)
    @Tag("mongoGetNoteRepository")
    void shouldFindActiveNotesOwnedByOrSharedWithOwner() {
        PrivateNoteDocument privateDocument = privateDocument();
        SharedNoteDocument sharedDocument = sharedDocument();
        Note privateNote = privateNote("507f1f77bcf86cd799439011");
        Note sharedNote = sharedNote();

        List<String> visibleStatuses = List.of("NEW", "READ");
        when(privateRepository.findAllByOwnerIdAndStatusIn("507f1f77bcf86cd799439012", visibleStatuses))
                .thenReturn(List.of(privateDocument));
        when(sharedRepository.findAllByOwnerIdAndStatusIn("507f1f77bcf86cd799439012", visibleStatuses))
                .thenReturn(List.of(sharedDocument));
        when(mapper.toDomain(privateDocument)).thenReturn(privateNote);
        when(mapper.toDomain(sharedDocument)).thenReturn(sharedNote);

        List<Note> result = repository.findActiveByOwner("507f1f77bcf86cd799439012");

        assertEquals(List.of(privateNote, sharedNote), result);
        verify(privateRepository).findAllByOwnerIdAndStatusIn("507f1f77bcf86cd799439012", visibleStatuses);
        verify(sharedRepository).findAllByOwnerIdAndStatusIn("507f1f77bcf86cd799439012", visibleStatuses);
        verify(sharedRepository).findAllByStatusInAndSharedWith(visibleStatuses, "507f1f77bcf86cd799439012");
    }

    @Test
    @Timeout(1)
    @Tag("mongoGetNoteRepository")
    void shouldFindActiveSharedNotesByRecipient() {
        SharedNoteDocument document = sharedDocument();
        Note note = sharedNote();

        when(sharedRepository.findAllByStatusAndSharedWith("NEW", "507f1f77bcf86cd799439013"))
                .thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(note);

        List<Note> result = repository.findActiveBySharedWith("507f1f77bcf86cd799439013");

        assertEquals(List.of(note), result);
        verify(sharedRepository).findAllByStatusAndSharedWith("NEW", "507f1f77bcf86cd799439013");
    }

    @Test
    @Timeout(1)
    @Tag("mongoGetNoteRepository")
    void shouldFindActiveSharedNotesByOwnerAndRecipient() {
        SharedNoteDocument document = sharedDocument();
        Note note = sharedNote();

        when(sharedRepository.findAllByStatusAndOwnerIdAndSharedWith(
                "NEW",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        )).thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(note);

        List<Note> result = repository.findActiveByOwnerAndSharedWith(
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );

        assertEquals(List.of(note), result);
        verify(sharedRepository).findAllByStatusAndOwnerIdAndSharedWith(
                "NEW",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
    }

    @Test
    @Timeout(1)
    @Tag("mongoDeleteNoteRepository")
    void shouldReturnNoteWhenDocumentExistsBeforeDeletion() {
        PrivateNoteDocument document = new PrivateNoteDocument(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "NEW",
                "2026-01-01T10:00:00Z",
                null,
                null,
                null,
                "507f1f77bcf86cd799439012"
        );
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");
        Note expectedNote = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(expectedNote);

        Optional<Note> result = repository.findById(new NoteId("507f1f77bcf86cd799439011"));

        assertTrue(result.isPresent());
        assertEquals(expectedNote, result.get());
        verify(mapper).toObjectId("507f1f77bcf86cd799439011");
        verify(baseRepository).findById(objectId);
        verify(mapper).toDomain(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoDeleteNoteRepository")
    void shouldReturnEmptyWhenDocumentDoesNotExistBeforeDeletion() {
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.empty());

        Optional<Note> result = repository.findById(new NoteId("507f1f77bcf86cd799439011"));

        assertTrue(result.isEmpty());
        verify(mapper, never()).toDomain(any());
        verify(baseRepository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("mongoDeleteNoteRepository")
    void shouldUpdateStatusWhenNoteIsSaved() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        note.delete();

        PrivateNoteDocument document = new PrivateNoteDocument(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "NEW",
                note.createdAt().value().toString(),
                null,
                null,
                null,
                "507f1f77bcf86cd799439012"
        );
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.of(document));

        repository.save(note);

        assertEquals("DELETED", document.getStatus());
        verify(baseRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoDeleteNoteRepository")
    void shouldInsertNewDocumentWhenSavingMissingNoteBeforeDeletion() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        note.delete();

        PrivateNoteDocument document = new PrivateNoteDocument(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "DELETED",
                note.createdAt().value().toString(),
                null,
                null,
                null,
                "507f1f77bcf86cd799439012"
        );
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.empty());
        when(mapper.toDocument(note)).thenReturn(document);

        repository.save(note);

        verify(baseRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoMarkNoteAsReadRepository")
    void shouldReturnNoteWhenDocumentExistsBeforeMarkingAsRead() {
        PrivateNoteDocument document = privateDocument("NEW", null);
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");
        Note expectedNote = privateNote();

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(expectedNote);

        Optional<Note> result = repository.findById(new NoteId("507f1f77bcf86cd799439011"));

        assertTrue(result.isPresent());
        assertEquals(expectedNote, result.get());
        verify(mapper).toObjectId("507f1f77bcf86cd799439011");
        verify(baseRepository).findById(objectId);
        verify(mapper).toDomain(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoMarkNoteAsReadRepository")
    void shouldReturnEmptyWhenDocumentDoesNotExistBeforeMarkingAsRead() {
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.empty());

        Optional<Note> result = repository.findById(new NoteId("507f1f77bcf86cd799439011"));

        assertTrue(result.isEmpty());
        verify(mapper, never()).toDomain(any());
        verify(baseRepository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("mongoMarkNoteAsReadRepository")
    void shouldUpdateStatusAndReadAtWhenNoteIsSaved() {
        Note note = privateNote();
        note.markAsRead();
        PrivateNoteDocument document = privateDocument("NEW", null);
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.of(document));

        repository.save(note);

        assertEquals("READ", document.getStatus());
        assertEquals(note.readAt().value().toString(), document.getReadAt());
        verify(baseRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoMarkNoteAsReadRepository")
    void shouldInsertNewDocumentWhenSavingMissingNoteBeforeMarkingAsRead() {
        Note note = privateNote();
        note.markAsRead();
        PrivateNoteDocument document = privateDocument("READ", note.readAt().value().toString());
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.empty());
        when(mapper.toDocument(note)).thenReturn(document);

        repository.save(note);

        verify(baseRepository).save(document);
    }

    private Note privateNote() {
        return new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
    }

    private Note sharedNote() {
        return new NoteFactory().createSharedNote(
                "507f1f77bcf86cd799439014",
                "Shared",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
    }

    private Note privateNote(String id) {
        return new NoteFactory().createPrivateNote(
                id,
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
    }

    private PrivateNoteDocument privateDocument() {
        return new PrivateNoteDocument(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "NEW",
                "2026-01-01T10:00:00Z",
                null,
                null,
                null,
                "507f1f77bcf86cd799439012"
        );
    }

    private PrivateNoteDocument privateDocument(String status, String readAt) {
        return new PrivateNoteDocument(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                status,
                "2026-01-01T10:00:00Z",
                readAt,
                null,
                null,
                "507f1f77bcf86cd799439012"
        );
    }

    private SharedNoteDocument sharedDocument() {
        return new SharedNoteDocument(
                "507f1f77bcf86cd799439014",
                "Shared",
                "Body",
                "NEW",
                "2026-01-01T10:00:00Z",
                null,
                null,
                null,
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
    }
}
