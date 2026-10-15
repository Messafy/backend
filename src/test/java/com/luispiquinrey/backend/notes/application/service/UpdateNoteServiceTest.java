package com.luispiquinrey.backend.notes.application.service;
import com.luispiquinrey.backend.share.logging.Logger;

import com.luispiquinrey.backend.notes.application.dto.UpdateNoteCommand;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNoteServiceTest {

    @Mock
    private NoteRepository repository;

    @Mock
    private Logger log;

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
                new UpdateNoteCommand("Updated title", "Updated content", List.of("personal")),
                "507f1f77bcf86cd799439012"
        );

        assertEquals("Updated title", updated.title().title());
        assertEquals("Updated content", updated.content().content());
        assertEquals(List.of("personal"), updated.tags().stream().map(com.luispiquinrey.backend.notes.domain.model.Tag::tag).toList());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteService")
    void shouldKeepExistingTagsWhenTagsAreOmitted() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Original title",
                "Original content",
                "507f1f77bcf86cd799439012",
                List.of(new com.luispiquinrey.backend.notes.domain.model.Tag("existing"))
        );
        NoteId id = note.id();
        when(repository.findById(id)).thenReturn(Optional.of(note));

        Note updated = service.update(
                id.id(),
                new UpdateNoteCommand("Updated title", "Updated content", null),
                "507f1f77bcf86cd799439012"
        );

        assertEquals(List.of("existing"), updated.tags().stream().map(com.luispiquinrey.backend.notes.domain.model.Tag::tag).toList());
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
                        new UpdateNoteCommand("Title", "Content", null),
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
