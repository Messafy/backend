package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkNoteAsReadServiceTest {

    @Mock
    private NoteRepository repository;

    @InjectMocks
    private MarkNoteAsReadService service;

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadService")
    void shouldMarkExistingNoteAsReadForOwner() {
        Note note = privateNote();
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.of(note));

        service.markAsRead("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012");

        assertEquals(NoteStatus.READ, note.status());
        assertNotNull(note.readAt());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadService")
    void shouldMarkSharedNoteAsReadForRecipient() {
        Note note = sharedNote();
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.of(note));

        service.markAsRead("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439013");

        assertEquals(NoteStatus.READ, note.status());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadService")
    void shouldRejectStrangerMarkingNoteAsRead() {
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.of(privateNote()));

        assertThrows(
                NoteAccessDeniedException.class,
                () -> service.markAsRead("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439014")
        );
        verify(repository, never()).save(new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011", "Hello", "Body", "507f1f77bcf86cd799439012"));
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadService")
    void shouldThrowNoteNotFoundWhenMissing() {
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> service.markAsRead("507f1f77bcf86cd799439011", "507f1f77bcf86cd799439012")
        );
        verify(repository, never()).save(new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011", "Hello", "Body", "507f1f77bcf86cd799439012"));
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
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );
    }
}
