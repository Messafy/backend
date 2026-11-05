package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.notes.application.dto.request.GetNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.request.ListNotesRequest;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.NotePage;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.notes.domain.repository.PageQuery;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNoteUseCaseTest {

    private static final String NOTE_ID = "note-1";
    private static final String OWNER_ID = "owner";
    private static final String RECIPIENT_ID = "recipient";

    @Mock
    private NoteRepository repository;

    @Mock
    private Logger log;

    @InjectMocks
    private GetNoteUseCase useCase;

    @Test
    @Timeout(1)
    @Tag("getNoteUseCase")
    void returnsAccessiblePrivateNoteToItsOwner() {
        Note note = new NoteFactory().createPrivateNote(NOTE_ID, "Private", "Body", OWNER_ID);
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.findById(new GetNoteRequest(NOTE_ID, OWNER_ID));

        assertTrue(result.isSuccess());
        assertEquals(NOTE_ID, result.getValue().id());
        assertEquals("PRIVATE", result.getValue().type());
        assertNull(result.getValue().sharedWith());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteUseCase")
    void hidesPrivateNotesFromOtherAccounts() {
        Note note = new NoteFactory().createPrivateNote(NOTE_ID, "Private", "Body", OWNER_ID);
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        var result = useCase.findById(new GetNoteRequest(NOTE_ID, "stranger"));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.NOT_FOUND_ERROR, result.getError().type());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteUseCase")
    void returnsSharedNoteToOwnerAndRecipient() {
        Note note = new NoteFactory().createSharedNote(NOTE_ID, "Shared", "Body", OWNER_ID, RECIPIENT_ID);
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.of(note));

        assertTrue(useCase.findById(new GetNoteRequest(NOTE_ID, OWNER_ID)).isSuccess());
        var recipient = useCase.findById(new GetNoteRequest(NOTE_ID, RECIPIENT_ID));
        assertTrue(recipient.isSuccess());
        assertEquals("SHARED", recipient.getValue().type());
        assertEquals(RECIPIENT_ID, recipient.getValue().sharedWith());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteUseCase")
    void rejectsMissingNote() {
        when(repository.findById(new NoteId(NOTE_ID))).thenReturn(Optional.empty());

        var result = useCase.findById(new GetNoteRequest(NOTE_ID, OWNER_ID));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.NOT_FOUND_ERROR, result.getError().type());
        assertEquals("note not found: note-1", result.getError().message());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteUseCase")
    void delegatesStatusQueriesForTheRequesterToTheRepository() {
        Note note = new NoteFactory().createPrivateNote(NOTE_ID, "Private", "Body", OWNER_ID);
        PageQuery page = new PageQuery(0, 20);
        when(repository.findAccessibleByStatus(OWNER_ID, NoteStatus.NEW, page))
                .thenReturn(new NotePage(List.of(note), page, 1));

        var owner = useCase.findByStatus(new ListNotesRequest(OWNER_ID, NoteStatus.NEW, null, page));

        assertEquals(1, owner.getValue().items().size());
        assertEquals(1, owner.getValue().totalItems());
        assertEquals(20, owner.getValue().size());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteUseCase")
    void delegatesOwnerSharedWithTrashAndOwnerRecipientQueries() {
        Note owned = new NoteFactory().createPrivateNote("note-owned", "Owned", "Body", OWNER_ID);
        Note shared = new NoteFactory().createSharedNote("note-shared", "Shared", "Body", OWNER_ID, RECIPIENT_ID);
        Note deleted = new NoteFactory().createPrivateNote("note-deleted", "Deleted", "Body", OWNER_ID);
        deleted.delete();
        PageQuery all = PageQuery.unpaged();
        when(repository.findActiveByOwner(OWNER_ID, all)).thenReturn(new NotePage(List.of(owned), all, 1));
        when(repository.findActiveBySharedWith(RECIPIENT_ID, all)).thenReturn(new NotePage(List.of(shared), all, 1));
        when(repository.findActiveByOwnerAndSharedWith(OWNER_ID, RECIPIENT_ID, all))
                .thenReturn(new NotePage(List.of(shared), all, 1));
        when(repository.findDeletedByOwner(OWNER_ID, all)).thenReturn(new NotePage(List.of(deleted), all, 1));

        assertEquals(1, useCase.findActiveByOwner(new ListNotesRequest(OWNER_ID, null, null)).getValue().items().size());
        assertEquals(1, useCase.findActiveBySharedWith(new ListNotesRequest(RECIPIENT_ID, null, null)).getValue().items().size());
        var combined = useCase.findActiveByOwnerAndSharedWith(
                new ListNotesRequest(OWNER_ID, null, RECIPIENT_ID));
        assertEquals(1, combined.getValue().items().size());
        assertEquals("SHARED", combined.getValue().items().getFirst().type());
        assertEquals(1, useCase.findDeletedByOwner(new ListNotesRequest(OWNER_ID, null, null)).getValue().items().size());
    }

    @Test
    @Timeout(1)
    @Tag("getNoteUseCase")
    void rejectsNullRequestsWithoutTouchingTheRepository() {
        assertTrue(useCase.findById(null).isFailure());
        assertTrue(useCase.findByStatus(null).isFailure());
        assertTrue(useCase.findActiveByOwner(null).isFailure());
        assertTrue(useCase.findActiveBySharedWith(null).isFailure());
        assertTrue(useCase.findActiveByOwnerAndSharedWith(null).isFailure());
        assertTrue(useCase.findDeletedByOwner(null).isFailure());
        verifyNoInteractions(repository);
    }

    @Test
    @Timeout(1)
    @Tag("getNoteUseCase")
    void requestValidationRejectsMissingIdentities() {
        assertThrows(IllegalArgumentException.class, () -> new GetNoteRequest(" ", OWNER_ID));
        assertThrows(IllegalArgumentException.class, () -> new GetNoteRequest(NOTE_ID, " "));
        assertThrows(IllegalArgumentException.class, () -> new ListNotesRequest(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new ListNotesRequest(OWNER_ID, null, " "));
    }
}
