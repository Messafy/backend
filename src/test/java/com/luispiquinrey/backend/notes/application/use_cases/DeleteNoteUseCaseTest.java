package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.DeleteNoteRequest;
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
class DeleteNoteUseCaseTest {

    private static final String NOTE_ID = "507f1f77bcf86cd799439011";
    private static final String OWNER_ID = "507f1f77bcf86cd799439012";
    private static final String STRANGER_ID = "507f1f77bcf86cd799439013";

    @Mock
    private NoteRepository repository;

    @Mock
    private Logger log;

    @InjectMocks
    private DeleteNoteUseCase useCase;

    @Test
    @Timeout(1)
    @Tag("deleteNoteUseCase")
    void marksOwnedNoteAsDeleted() {
        Note note = privateNote();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.delete(new DeleteNoteRequest(NOTE_ID, OWNER_ID));

        assertTrue(result.isSuccess());
        assertEquals(NOTE_ID, result.getValue().id());
        assertEquals(NoteStatus.DELETED, note.status());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteUseCase")
    void rejectsMissingNote() {
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.empty());

        var result = useCase.delete(new DeleteNoteRequest(NOTE_ID, OWNER_ID));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.NOT_FOUND_ERROR, result.getError().type());
        assertEquals("note not found: " + NOTE_ID, result.getError().message());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteUseCase")
    void rejectsNonOwnerWithoutSaving() {
        Note note = privateNote();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.delete(new DeleteNoteRequest(NOTE_ID, STRANGER_ID));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.FORBIDDEN_ERROR, result.getError().type());
        assertEquals("you cannot delete this note", result.getError().message());
        assertEquals(NoteStatus.NEW, note.status());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteUseCase")
    void deletingAnAlreadyDeletedNoteIsIdempotent() {
        Note note = privateNote();
        note.delete();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.delete(new DeleteNoteRequest(NOTE_ID, OWNER_ID));

        assertTrue(result.isSuccess());
        assertEquals(NoteStatus.DELETED, note.status());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("deleteNoteUseCase")
    void rejectsNullRequest() {
        var result = useCase.delete(null);

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
    }

    private Note privateNote() {
        return new NoteFactory().createPrivateNote(NOTE_ID, "Hello", "Body", OWNER_ID);
    }
}
