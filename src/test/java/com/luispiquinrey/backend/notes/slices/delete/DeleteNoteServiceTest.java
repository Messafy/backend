package com.luispiquinrey.backend.notes.slices.delete;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteNoteServiceTest {

    @Mock
    private DeleteNoteRepository repository;

    @InjectMocks
    private DeleteNoteService service;

    @Test
    @Timeout(1)
    @Tag("deleteNoteService")
    void shouldMarkExistingNoteAsDeleted() {
        Note note = new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011",
                "Hello",
                "Body",
                "507f1f77bcf86cd799439012"
        );
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.of(note));

        service.delete(
                "507f1f77bcf86cd799439011",
                "507f1f77bcf86cd799439012"
        );

        assertEquals(NoteStatus.DELETED, note.status());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteService")
    void shouldThrowNoteNotFoundWhenMissing() {
        when(repository.findById(new NoteId("507f1f77bcf86cd799439011")))
                .thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> service.delete(
                        "507f1f77bcf86cd799439011",
                        "507f1f77bcf86cd799439012"
                )
        );
        verify(repository, never()).save(new NoteFactory().createPrivateNote(
                "507f1f77bcf86cd799439011", "Hello", "Body", "507f1f77bcf86cd799439012"));
    }
}
