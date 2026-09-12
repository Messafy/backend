package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.infrastructure.MongoDataNoteRepository;
import com.luispiquinrey.backend.notes.infrastructure.NoteMapper;
import com.luispiquinrey.backend.notes.infrastructure.PrivateNoteDocument;
import com.luispiquinrey.backend.notes.infrastructure.SharedNoteDocument;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoCreateNoteRepositoryTest {

    @Mock
    private NoteMapper mapper;

    @Mock
    private MongoDataNoteRepository baseRepository;

    @InjectMocks
    private MongoCreateNoteRepository repository;

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
}
