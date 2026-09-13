package com.luispiquinrey.backend.notes.slices.update;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteFactory;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNoteServiceTest {

    @Mock
    private UpdateNoteRepository repository;

    @InjectMocks
    private UpdateNoteService service;

    @Test
    @Timeout(1)
    @Tag("updateNoteService")
    void shouldUpdateExistingNote() {
        Note note = privateNote();
        NoteId id = new NoteId("507f1f77bcf86cd799439011");
        when(repository.findById(id)).thenReturn(Optional.of(note));

        Note updated = service.update(
                id.id(),
                new NoteUpdateRequest("Updated title", "Updated content"),
                "507f1f77bcf86cd799439012"
        );

        assertEquals("Updated title", updated.title().title());
        assertEquals("Updated content", updated.content().content());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteService")
    void shouldThrowWhenNoteDoesNotExist() {
        NoteId id = new NoteId("507f1f77bcf86cd799439011");
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> service.update(
                        id.id(),
                        new NoteUpdateRequest("Title", "Content"),
                        "507f1f77bcf86cd799439012"
                )
        );
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Note privateNote() {
        return new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Original title",
                "Original content",
                "507f1f77bcf86cd799439012"
        );
    }
}
