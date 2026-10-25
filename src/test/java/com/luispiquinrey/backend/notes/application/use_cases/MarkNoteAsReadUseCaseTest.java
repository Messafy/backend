package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.MarkNoteAsReadRequest;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ErrorType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
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
class MarkNoteAsReadUseCaseTest {

    private static final String NOTE_ID = "507f1f77bcf86cd799439011";
    private static final String OWNER_ID = "507f1f77bcf86cd799439012";
    private static final String RECIPIENT_ID = "507f1f77bcf86cd799439013";
    private static final String STRANGER_ID = "507f1f77bcf86cd799439014";

    @Mock
    private NoteRepository repository;

    @Mock
    private Logger log;

    @InjectMocks
    private MarkNoteAsReadUseCase useCase;

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadUseCase")
    void marksOwnedNoteAsRead() {
        Note note = new NoteFactory().createPrivateNote(NOTE_ID, "Hello", "Body", OWNER_ID);
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.markAsRead(new MarkNoteAsReadRequest(NOTE_ID, OWNER_ID));

        assertTrue(result.isSuccess());
        assertEquals(NOTE_ID, result.getValue().id());
        assertNotNull(result.getValue().readAt());
        assertEquals(NoteStatus.READ, note.status());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadUseCase")
    void marksSharedNoteAsReadForRecipient() {
        Note note = new NoteFactory().createSharedNote(NOTE_ID, "Hello", "Body", OWNER_ID, RECIPIENT_ID);
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.markAsRead(new MarkNoteAsReadRequest(NOTE_ID, RECIPIENT_ID));

        assertTrue(result.isSuccess());
        assertEquals(NoteStatus.READ, note.status());
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadUseCase")
    void rejectsStrangerWithoutSaving() {
        Note note = new NoteFactory().createPrivateNote(NOTE_ID, "Hello", "Body", OWNER_ID);
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.markAsRead(new MarkNoteAsReadRequest(NOTE_ID, STRANGER_ID));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.FORBIDDEN_ERROR, result.getError().type());
        assertEquals(NoteStatus.NEW, note.status());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadUseCase")
    void rejectsMissingNote() {
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.empty());

        var result = useCase.markAsRead(new MarkNoteAsReadRequest(NOTE_ID, OWNER_ID));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.NOT_FOUND_ERROR, result.getError().type());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadUseCase")
    void rejectsDeletedNoteAsConflict() {
        Note note = new NoteFactory().createPrivateNote(NOTE_ID, "Hello", "Body", OWNER_ID);
        note.delete();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.markAsRead(new MarkNoteAsReadRequest(NOTE_ID, OWNER_ID));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.CONFLICT_ERROR, result.getError().type());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("markNoteAsReadUseCase")
    void rejectsNullRequest() {
        var result = useCase.markAsRead(null);

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
    }
}
