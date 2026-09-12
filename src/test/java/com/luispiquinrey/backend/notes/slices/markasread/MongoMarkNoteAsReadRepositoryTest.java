package com.luispiquinrey.backend.notes.slices.markasread;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.notes.infrastructure.MongoDataNoteRepository;
import com.luispiquinrey.backend.notes.infrastructure.NoteMapper;
import com.luispiquinrey.backend.notes.infrastructure.PrivateNoteDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoMarkNoteAsReadRepositoryTest {

    @Mock
    private NoteMapper mapper;

    @Mock
    private MongoDataNoteRepository baseRepository;

    @InjectMocks
    private MongoMarkNoteAsReadRepository repository;

    @Test
    @Timeout(1)
    @Tag("mongoMarkNoteAsReadRepository")
    void shouldReturnNoteWhenDocumentExists() {
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
    void shouldReturnEmptyWhenDocumentDoesNotExist() {
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
    void shouldThrowNoteNotFoundWhenSavingMissingNote() {
        Note note = privateNote();
        note.markAsRead();
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(baseRepository.findById(objectId)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> repository.save(note));

        verify(baseRepository, never()).save(any());
    }

    private Note privateNote() {
        return new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
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
}
