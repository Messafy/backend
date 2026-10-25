package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.request.ShareNoteRequest;
import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.identity.UserId;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareNoteUseCaseTest {

    private static final String NOTE_ID = "note-1";
    private static final String OWNER_ID = "owner";
    private static final String PUBLIC_CODE = "V1StGXR8_Z5j";
    private static final String LOCKER_PASSWORD = "CorrectLocker1!";

    @Mock
    private NoteRepository repository;

    @Mock
    private AccountLookup accounts;

    @Mock
    private NoteRealtimeNotifier notifier;

    @Mock
    private Logger log;

    @InjectMocks
    private ShareNoteUseCase useCase;

    @Test
    @Timeout(1)
    @Tag("shareNoteUseCase")
    void savesSharedRepresentationBeforeNotifying() {
        Note original = prepareNote();

        var result = useCase.share(request());

        assertTrue(result.isSuccess());
        assertEquals("SHARED", result.getValue().type());
        assertEquals("recipient", result.getValue().sharedWith());
        var order = inOrder(repository, notifier);
        order.verify(repository).findById(original.id());
        order.verify(repository).save(any());
        order.verify(notifier).notifySharedNote(any(), org.mockito.ArgumentMatchers.eq("recipient"));
        order.verifyNoMoreInteractions();
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteUseCase")
    void failedSaveDoesNotNotifyOrMutateOriginalNote() {
        Note original = prepareNote();
        doThrow(new IllegalStateException("database unavailable")).when(repository).save(any());

        assertThrows(IllegalStateException.class, () -> useCase.share(request()));

        verifyNoInteractions(notifier);
        assertEquals("Body", original.content().content());
        assertEquals("NEW", original.status().name());
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteUseCase")
    void rejectsNonOwnerWithoutSavingOrNotifying() {
        Note original = new NoteFactory().createPrivateNote(NOTE_ID, "Title", "Body", OWNER_ID);
        when(repository.findById(original.id())).thenReturn(Optional.of(original));

        var result = useCase.share(new ShareNoteRequest(NOTE_ID, "stranger", PUBLIC_CODE, LOCKER_PASSWORD));

        assertTrue(result.isFailure());
        assertEquals(ErrorType.FORBIDDEN_ERROR, result.getError().type());
        verify(repository, never()).save(any());
        verifyNoInteractions(notifier);
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteUseCase")
    void rejectsAlreadySharedNoteAsConflict() {
        Note shared = new NoteFactory().createSharedNote(NOTE_ID, "Title", "Body", OWNER_ID, "recipient");
        when(repository.findById(shared.id())).thenReturn(Optional.of(shared));

        var result = useCase.share(request());

        assertTrue(result.isFailure());
        assertEquals(ErrorType.CONFLICT_ERROR, result.getError().type());
        assertEquals("only private notes can be shared", result.getError().message());
        verify(repository, never()).save(any());
        verifyNoInteractions(accounts, notifier);
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteUseCase")
    void rejectsUnknownRecipientWithoutSaving() {
        when(repository.findById(new com.luispiquinrey.backend.notes.domain.model.NoteId(NOTE_ID)))
                .thenReturn(Optional.of(new NoteFactory().createPrivateNote(NOTE_ID, "Title", "Body", OWNER_ID)));
        when(accounts.findActiveAccountIdByPublicCode(PUBLIC_CODE)).thenReturn(Optional.empty());

        var result = useCase.share(request());

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
        verify(repository, never()).save(any());
        verifyNoInteractions(notifier);
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteUseCase")
    void rejectsLockerPasswordMismatchWithoutSaving() {
        when(repository.findById(new com.luispiquinrey.backend.notes.domain.model.NoteId(NOTE_ID)))
                .thenReturn(Optional.of(new NoteFactory().createPrivateNote(NOTE_ID, "Title", "Body", OWNER_ID)));
        when(accounts.findActiveAccountIdByPublicCode(PUBLIC_CODE))
                .thenReturn(Optional.of(new UserId("recipient")));
        when(accounts.matchesLockerPassword(new UserId("recipient"), LOCKER_PASSWORD)).thenReturn(false);

        var result = useCase.share(request());

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
        verify(repository, never()).save(any());
        verifyNoInteractions(notifier);
    }

    @Test
    @Timeout(1)
    @Tag("shareNoteUseCase")
    void rejectsMissingNoteAndNullRequest() {
        when(repository.findById(new com.luispiquinrey.backend.notes.domain.model.NoteId(NOTE_ID)))
                .thenReturn(Optional.empty());

        var missing = useCase.share(request());
        assertTrue(missing.isFailure());
        assertEquals(ErrorType.NOT_FOUND_ERROR, missing.getError().type());

        var nullRequest = useCase.share(null);
        assertTrue(nullRequest.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, nullRequest.getError().type());
    }

    private ShareNoteRequest request() {
        return new ShareNoteRequest(NOTE_ID, OWNER_ID, PUBLIC_CODE, LOCKER_PASSWORD);
    }

    private Note prepareNote() {
        Note original = new NoteFactory().createPrivateNote(NOTE_ID, "Title", "Body", OWNER_ID);
        when(repository.findById(original.id())).thenReturn(Optional.of(original));
        when(accounts.findActiveAccountIdByPublicCode(PUBLIC_CODE)).thenReturn(Optional.of(new UserId("recipient")));
        when(accounts.matchesLockerPassword(new UserId("recipient"), LOCKER_PASSWORD)).thenReturn(true);
        return original;
    }
}
