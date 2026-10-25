package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.PinNoteRequest;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ErrorType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PinNoteUseCaseTest {

    private static final String NOTE_ID = "note";
    private static final String OWNER_ID = "owner";
    private static final String STRANGER_ID = "stranger";

    @Mock
    private NoteRepository repository;

    @Mock
    private Logger log;

    @InjectMocks
    private PinNoteUseCase useCase;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @Timeout(1)
    @Tag("pinNoteUseCase")
    void ownerCanPinAndUnpin(boolean pinned) {
        Note note = note();
        note.setPinned(!pinned);
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.setPinned(new PinNoteRequest(NOTE_ID, OWNER_ID, pinned));

        assertTrue(result.isSuccess());
        assertTrue(result.getValue().pinned() == pinned);
        assertEquals(pinned, note.pinned());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("pinNoteUseCase")
    void rejectsNullRequestAndNullFlag() {
        assertTrue(useCase.setPinned(null).isFailure());
        assertThrows(IllegalArgumentException.class, () -> new PinNoteRequest(NOTE_ID, OWNER_ID, null));
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("pinNoteUseCase")
    void rejectsMissingNote() {
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.empty());

        var result = useCase.setPinned(new PinNoteRequest(NOTE_ID, OWNER_ID, true));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.NOT_FOUND_ERROR, result.getError().type());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("pinNoteUseCase")
    void rejectsNonOwnerWithoutMutatingOrSaving() {
        Note note = note();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.setPinned(new PinNoteRequest(NOTE_ID, STRANGER_ID, true));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.FORBIDDEN_ERROR, result.getError().type());
        assertFalse(note.pinned());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("pinNoteUseCase")
    void rejectsDeletedNoteWithoutSaving() {
        Note note = note();
        note.delete();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.setPinned(new PinNoteRequest(NOTE_ID, OWNER_ID, true));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.CONFLICT_ERROR, result.getError().type());
        assertFalse(note.pinned());
        verify(repository, never()).save(any());
    }

    private Note note() {
        return new NoteFactory().createPrivateNote(NOTE_ID, "Title", "Content", OWNER_ID);
    }
}
