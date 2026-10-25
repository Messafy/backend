package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.UpdateNoteRequest;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNoteUseCaseTest {

    private static final String NOTE_ID = "507f1f77bcf86cd799439011";
    private static final String OWNER_ID = "507f1f77bcf86cd799439012";
    private static final String STRANGER_ID = "507f1f77bcf86cd799439013";

    @Mock
    private NoteRepository repository;

    @Mock
    private Logger log;

    @InjectMocks
    private UpdateNoteUseCase useCase;

    @Test
    @Timeout(1)
    @Tag("updateNoteUseCase")
    void updatesTitleContentAndTags() {
        Note note = privateNote();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.update(new UpdateNoteRequest(
                NOTE_ID, OWNER_ID, "Updated title", "Updated content", List.of("personal")));

        assertTrue(result.isSuccess());
        assertEquals("Updated title", result.getValue().title());
        assertEquals("Updated content", result.getValue().content());
        assertEquals("Updated title", note.title().title());
        assertEquals(List.of("personal"), note.tags().stream().map(com.luispiquinrey.backend.notes.domain.model.Tag::tag).toList());
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteUseCase")
    void keepsExistingTagsWhenTagsAreOmitted() {
        Note note = new NoteFactory().createPrivateNote(
                NOTE_ID, "Original title", "Original content", OWNER_ID,
                List.of(new com.luispiquinrey.backend.notes.domain.model.Tag("existing")));
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.update(new UpdateNoteRequest(
                NOTE_ID, OWNER_ID, "Updated title", "Updated content", null));

        assertTrue(result.isSuccess());
        assertEquals(List.of("existing"), note.tags().stream().map(com.luispiquinrey.backend.notes.domain.model.Tag::tag).toList());
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteUseCase")
    void rejectsMissingNote() {
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.empty());

        var result = useCase.update(new UpdateNoteRequest(
                NOTE_ID, OWNER_ID, "Title", "Content", null));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.NOT_FOUND_ERROR, result.getError().type());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteUseCase")
    void rejectsNonOwnerWithoutSaving() {
        Note note = privateNote();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.update(new UpdateNoteRequest(
                NOTE_ID, STRANGER_ID, "Title", "Content", null));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.FORBIDDEN_ERROR, result.getError().type());
        assertEquals("Original title", note.title().title());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteUseCase")
    void rejectsSharedAndDeletedNotesAsConflicts() {
        Note shared = new NoteFactory().createSharedNote(NOTE_ID, "Shared", "Body", OWNER_ID, "recipient");
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(shared));
        var sharedResult = useCase.update(new UpdateNoteRequest(
                NOTE_ID, OWNER_ID, "Title", "Content", null));
        assertTrue(sharedResult.isFailure());
        assertEquals(ErrorType.CONFLICT_ERROR, sharedResult.getError().type());

        Note deleted = privateNote();
        deleted.delete();
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(deleted));
        var deletedResult = useCase.update(new UpdateNoteRequest(
                NOTE_ID, OWNER_ID, "Title", "Content", null));
        assertTrue(deletedResult.isFailure());
        assertEquals(ErrorType.CONFLICT_ERROR, deletedResult.getError().type());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @Tag("updateNoteUseCase")
    void rejectsNullRequest() {
        var result = useCase.update(null);

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
    }

    private Note privateNote() {
        return new NoteFactory().createPrivateNote(NOTE_ID, "Original title", "Original content", OWNER_ID);
    }
}
