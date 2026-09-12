package com.luispiquinrey.backend.notes.slices.get;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNoteServiceTest {

    @Mock
    private GetNoteRepository repository;

    @InjectMocks
    private GetNoteService service;

    @Test
    @Timeout(1)
    @Tag("getNoteService")
    void shouldReturnNoteWhenFoundById() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.of(note));

        Optional<Note> result = service.findById("507f1f77bcf86cd799439011");

        assertTrue(result.isPresent());
        assertSame(note, result.get());
        verify(repository).findById(new NoteId("507f1f77bcf86cd799439011"));
    }

    @Test
    @Timeout(1)
    @Tag("getNoteService")
    void shouldReturnEmptyWhenNoteIsMissing() {
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.empty());

        Optional<Note> result = service.findById("507f1f77bcf86cd799439011");

        assertTrue(result.isEmpty());
        verify(repository).findById(new NoteId("507f1f77bcf86cd799439011"));
    }

    @Test
    @Timeout(1)
    @Tag("getNoteService")
    void shouldDelegateFindByStatusToRepository() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        when(repository.findByStatus("NEW")).thenReturn(List.of(note));

        List<Note> result = service.findByStatus(NoteStatus.NEW);

        assertEquals(List.of(note), result);
        verify(repository).findByStatus("NEW");
    }

    @Test
    @Timeout(1)
    @Tag("getNoteService")
    void shouldDelegateFindActiveByOwnerToRepository() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        when(repository.findActiveByOwner("507f1f77bcf86cd799439012"))
                .thenReturn(List.of(note));

        List<Note> result = service.findActiveByOwner("507f1f77bcf86cd799439012");

        assertEquals(List.of(note), result);
        verify(repository).findActiveByOwner("507f1f77bcf86cd799439012");
    }

    @Test
    @Timeout(1)
    @Tag("getNoteService")
    void shouldDelegateFindActiveBySharedWithToRepository() {
        Note note = new NoteFactory().createSharedNote(
                "507f1f77bcf86cd799439014",
                "Shared",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
        when(repository.findActiveBySharedWith("507f1f77bcf86cd799439013"))
                .thenReturn(List.of(note));

        List<Note> result = service.findActiveBySharedWith("507f1f77bcf86cd799439013");

        assertEquals(List.of(note), result);
        verify(repository).findActiveBySharedWith("507f1f77bcf86cd799439013");
    }

    @Test
    @Timeout(1)
    @Tag("getNoteService")
    void shouldDelegateFindActiveByOwnerAndSharedWithToRepository() {
        Note note = new NoteFactory().createSharedNote(
                "507f1f77bcf86cd799439014",
                "Shared",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
        when(repository.findActiveByOwnerAndSharedWith(
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        )).thenReturn(List.of(note));

        List<Note> result = service.findActiveByOwnerAndSharedWith(
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );

        assertEquals(List.of(note), result);
        verify(repository).findActiveByOwnerAndSharedWith(
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
    }

    @Test
    @Timeout(1)
    @Tag("getNoteService")
    void shouldReturnEmptyListWhenOwnerHasNoActiveNotes() {
        when(repository.findActiveByOwner("507f1f77bcf86cd799439012"))
                .thenReturn(List.of());

        List<Note> result = service.findActiveByOwner("507f1f77bcf86cd799439012");

        assertTrue(result.isEmpty());
        verify(repository).findActiveByOwner("507f1f77bcf86cd799439012");
    }
}
