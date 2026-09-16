package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import com.luispiquinrey.backend.notes.domain.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.PrivateNote;
import com.luispiquinrey.backend.notes.domain.SharedNote;
import org.junit.jupiter.api.Tag;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNoteServiceTest {

    @Mock
    private CreateNoteRepository repository;

    @Mock
    private AccountLookup accountLookup;

    @InjectMocks
    private CreateNoteService service;

    @Test
    @Timeout(1)
    @Tag("createNoteService")
    void shouldCreatePrivateNoteFromDto() {
        NoteCreationRequest dto = new NoteCreationRequest(
                NoteCreationRequest.NoteType.PRIVATE,
                "Hello",
                "Body",
                null,
                List.of("work", "urgent")
        );

        Note note = service.createNote(dto, "507f1f77bcf86cd799439012");

        assertInstanceOf(PrivateNote.class, note);
        assertNotNull(note.id());
        assertEquals("Hello", note.title().title());
        assertEquals("Body", note.content().content());
        assertEquals(NoteStatus.NEW, note.status());
        assertEquals(List.of("work", "urgent"), note.tags().stream().map(com.luispiquinrey.backend.notes.domain.Tag::tag).toList());
        assertTrue(note.isOwnedBy("507f1f77bcf86cd799439012"));
        assertFalse(note.isOwnedBy("507f1f77bcf86cd799439013"));
        verifyNoInteractions(accountLookup);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repository).save(captor.capture());
        assertEquals(note.id().id(), captor.getValue().id().id());
    }

    @Test
    @Timeout(1)
    @Tag("createNoteService")
    void shouldCreateSharedNoteFromDto() {
        when(accountLookup.existsActiveAccount("507f1f77bcf86cd799439013")).thenReturn(true);

        NoteCreationRequest dto = new NoteCreationRequest(
                NoteCreationRequest.NoteType.SHARED,
                "Hello",
                "Body",
                "507f1f77bcf86cd799439013"
        );

        Note note = service.createNote(dto, "507f1f77bcf86cd799439012");

        assertInstanceOf(SharedNote.class, note);
        verify(accountLookup).existsActiveAccount("507f1f77bcf86cd799439013");
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("createNoteService")
    void shouldRejectSharedNoteWhenRecipientIsNotActive() {
        when(accountLookup.existsActiveAccount("507f1f77bcf86cd799439013")).thenReturn(false);

        NoteCreationRequest dto = new NoteCreationRequest(
                NoteCreationRequest.NoteType.SHARED,
                "Hello",
                "Body",
                "507f1f77bcf86cd799439013"
        );

        assertThrows(
                NoteValidationException.class,
                () -> service.createNote(dto, "507f1f77bcf86cd799439012")
        );
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("createNoteService")
    void shouldRejectSharedNoteWhenRecipientIsMissing() {
        NoteCreationRequest dto = new NoteCreationRequest(
                NoteCreationRequest.NoteType.SHARED,
                "Hello",
                "Body",
                null
        );

        assertThrows(
                NoteValidationException.class,
                () -> service.createNote(dto, "507f1f77bcf86cd799439012")
        );
        verifyNoInteractions(accountLookup);
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("createNoteService")
    void shouldThrowWhenDtoIsNull() {
        assertThrows(
                NoteValidationException.class,
                () -> service.createNote(null, "507f1f77bcf86cd799439012")
        );
    }

    @Test
    @Timeout(1)
    @Tag("createNoteService")
    void shouldThrowWhenTypeIsNull() {
        NoteCreationRequest dto = new NoteCreationRequest(
                null,
                "Hello",
                "Body",
                null
        );

        assertThrows(
                NoteValidationException.class,
                () -> service.createNote(dto, "507f1f77bcf86cd799439012")
        );
    }
}
