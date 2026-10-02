package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.application.dto.CreatePrivateNoteCommand;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.model.PrivateNote;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePrivateNoteServiceTest {

    @Mock
    private NoteRepository repository;

    @Mock
    private NoteIdGenerator idGenerator;

    @InjectMocks
    private CreatePrivateNoteService service;

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createPrivateNoteService")
    void shouldCreatePrivateNoteFromCommand() {
        when(idGenerator.nextId()).thenReturn("507f1f77bcf86cd799439011");
        CreatePrivateNoteCommand command = new CreatePrivateNoteCommand(
                "Hello",
                "Body",
                List.of("work", "urgent")
        );

        Note note = service.create(command, "507f1f77bcf86cd799439012");

        assertInstanceOf(PrivateNote.class, note);
        assertNotNull(note.id());
        assertEquals("Hello", note.title().title());
        assertEquals("Body", note.content().content());
        assertEquals(NoteStatus.NEW, note.status());
        assertEquals(
                List.of("work", "urgent"),
                note.tags().stream().map(Tag::tag).toList()
        );
        assertTrue(note.isOwnedBy("507f1f77bcf86cd799439012"));
        assertFalse(note.isOwnedBy("507f1f77bcf86cd799439013"));

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repository).save(captor.capture());
        assertEquals(note.id().id(), captor.getValue().id().id());
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createPrivateNoteService")
    void shouldCreatePrivateNoteWithoutTags() {
        when(idGenerator.nextId()).thenReturn("507f1f77bcf86cd799439011");
        CreatePrivateNoteCommand command = new CreatePrivateNoteCommand(
                "Hello",
                "Body",
                null
        );

        Note note = service.create(command, "507f1f77bcf86cd799439012");

        assertInstanceOf(PrivateNote.class, note);
        assertTrue(note.tags().isEmpty());
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createPrivateNoteService")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(
                NoteValidationException.class,
                () -> service.create(null, "507f1f77bcf86cd799439012")
        );
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createPrivateNoteService")
    void shouldThrowWhenAuthenticatedAccountIdIsBlank() {
        CreatePrivateNoteCommand command = new CreatePrivateNoteCommand(
                "Hello",
                "Body",
                null
        );

        assertThrows(
                NoteValidationException.class,
                () -> service.create(command, " ")
        );
    }
}