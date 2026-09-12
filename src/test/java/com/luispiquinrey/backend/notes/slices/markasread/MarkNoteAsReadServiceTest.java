package com.luispiquinrey.backend.notes.slices.markasread;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
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
    private MarkNoteAsReadRepository repository;

    @InjectMocks
    private MarkNoteAsReadService service;

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadService")
    void shouldMarkExistingNoteAsRead() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.of(note));

        service.markAsRead("507f1f77bcf86cd799439011");

        assertEquals(NoteStatus.READ, note.status());
        assertNotNull(note.readAt());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadService")
    void shouldThrowNoteNotFoundWhenMissing() {
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> service.markAsRead("507f1f77bcf86cd799439011")
        );
        verify(repository, never()).save(new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011", "Hello", "Body", "507f1f77bcf86cd799439012"));
    }
}
