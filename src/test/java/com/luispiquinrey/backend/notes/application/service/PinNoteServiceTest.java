package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.application.dto.PinNoteCommand;
import com.luispiquinrey.backend.notes.domain.exception.NoteAccessDeniedException;
import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.exception.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PinNoteServiceTest {
    @Mock private NoteRepository repository;
    @Mock private Logger log;
    @InjectMocks private PinNoteService service;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ownerCanPinAndUnpin(boolean pinned) {
        Note note = note();
        note.setPinned(!pinned);
        when(repository.findById(new NoteId("note"))).thenReturn(Optional.of(note));

        assertSame(note, service.setPinned("note", new PinNoteCommand(pinned), "owner"));

        assertEquals(pinned, note.pinned());
        verify(repository).save(note);
    }

    @Test
    void rejectsMissingPinDataBeforeAccessingRepository() {
        assertThrows(NoteValidationException.class, () -> service.setPinned("note", null, "owner"));
        assertThrows(NoteValidationException.class,
                () -> service.setPinned("note", new PinNoteCommand(null), "owner"));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMissingNote() {
        when(repository.findById(new NoteId("note"))).thenReturn(Optional.empty());
        assertThrows(NoteNotFoundException.class,
                () -> service.setPinned("note", new PinNoteCommand(true), "owner"));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsNonOwnerWithoutSavingOrMutatingNote() {
        Note note = note();
        when(repository.findById(new NoteId("note"))).thenReturn(Optional.of(note));
        assertThrows(NoteAccessDeniedException.class,
                () -> service.setPinned("note", new PinNoteCommand(true), "stranger"));
        assertFalse(note.pinned());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDeletedNoteWithoutSaving() {
        Note note = note();
        note.delete();
        when(repository.findById(new NoteId("note"))).thenReturn(Optional.of(note));
        assertThrows(NoteConflictException.class,
                () -> service.setPinned("note", new PinNoteCommand(true), "owner"));
        assertFalse(note.pinned());
        verify(repository, never()).save(any());
    }

    private Note note() {
        return new NoteFactory().createPrivateNote("note", "Title", "Content", "owner");
    }
}
