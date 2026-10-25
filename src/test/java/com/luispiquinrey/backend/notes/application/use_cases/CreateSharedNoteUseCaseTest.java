package com.luispiquinrey.backend.notes.application.use_cases;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.request.CreateSharedNoteRequest;
import com.luispiquinrey.backend.notes.application.dto.response.CreatedNoteResponse;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSharedNoteUseCaseTest {

    private static final String OWNER_ID = "507f1f77bcf86cd799439012";
    private static final String NOTE_ID = "507f1f77bcf86cd799439011";
    private static final String PUBLIC_CODE = "V1StGXR8_Z5j";
    private static final String LOCKER_PASSWORD = "Locker1!";
    private static final UserId RECIPIENT_ID = new UserId("507f1f77bcf86cd799439013");

    @Mock
    private NoteRepository repository;

    @Mock
    private AccountLookup accountLookup;

    @Mock
    private NoteIdGenerator idGenerator;

    @Mock
    private NoteRealtimeNotifier realtimeNotifier;

    @Mock
    private Logger log;

    @InjectMocks
    private CreateSharedNoteUseCase useCase;

    @Test
    @Timeout(1)
    @Tag("createSharedNoteUseCase")
    void createsSharedNoteForVerifiedRecipient() {
        when(accountLookup.findActiveAccountIdByPublicCode(PUBLIC_CODE)).thenReturn(Optional.of(RECIPIENT_ID));
        when(accountLookup.matchesLockerPassword(RECIPIENT_ID, "CorrectLocker!")).thenReturn(true);
        when(idGenerator.nextId()).thenReturn(NOTE_ID);
        var request = new CreateSharedNoteRequest(
                OWNER_ID, "Hello", "Body", PUBLIC_CODE, "CorrectLocker!", null);

        var result = useCase.create(request);

        assertTrue(result.isSuccess());
        CreatedNoteResponse response = result.getValue();
        assertEquals("SHARED", response.type());
        assertEquals("NEW", response.status());
        verify(accountLookup).findActiveAccountIdByPublicCode(PUBLIC_CODE);
        verify(accountLookup).matchesLockerPassword(RECIPIENT_ID, "CorrectLocker!");
        verify(repository).save(any(SharedNote.class));
        verify(realtimeNotifier).notifySharedNote(any(Note.class), eq(RECIPIENT_ID.id()));
    }

    @Test
    @Timeout(1)
    @Tag("createSharedNoteUseCase")
    void rejectsUnknownRecipientWithoutSaving() {
        when(accountLookup.findActiveAccountIdByPublicCode(PUBLIC_CODE)).thenReturn(Optional.empty());
        var request = new CreateSharedNoteRequest(
                OWNER_ID, "Hello", "Body", PUBLIC_CODE, "AnyLocker1!", null);

        var result = useCase.create(request);

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
        assertEquals("no active account was found for the provided public code", result.getError().message());
        verify(accountLookup, never()).matchesLockerPassword(any(), any());
        verify(repository, never()).save(any());
        verify(realtimeNotifier, never()).notifySharedNote(any(), any());
    }

    @Test
    @Timeout(1)
    @Tag("createSharedNoteUseCase")
    void rejectsLockerPasswordMismatchWithoutSaving() {
        when(accountLookup.findActiveAccountIdByPublicCode(PUBLIC_CODE)).thenReturn(Optional.of(RECIPIENT_ID));
        when(accountLookup.matchesLockerPassword(RECIPIENT_ID, "WrongLocker!")).thenReturn(false);
        var request = new CreateSharedNoteRequest(
                OWNER_ID, "Hello", "Body", PUBLIC_CODE, "WrongLocker!", null);

        var result = useCase.create(request);

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
        assertEquals("locker password does not match the recipient", result.getError().message());
        verify(repository, never()).save(any());
        verify(realtimeNotifier, never()).notifySharedNote(any(), any());
    }

    @Test
    @Timeout(1)
    @Tag("createSharedNoteUseCase")
    void rejectsNullRequest() {
        var result = useCase.create(null);

        assertTrue(result.isFailure());
        assertEquals("note creation data cannot be null", result.getError().message());
    }

    @Test
    @Timeout(1)
    @Tag("createSharedNoteUseCase")
    void unknownRecipientIsRejectedByTheAccountLookup() {
        when(accountLookup.findActiveAccountIdByPublicCode(PUBLIC_CODE)).thenReturn(Optional.empty());
        var request = new CreateSharedNoteRequest(
                OWNER_ID, "Title", "Body", PUBLIC_CODE, LOCKER_PASSWORD, null);

        var result = useCase.create(request);

        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
        verify(repository, never()).save(any());
        verify(realtimeNotifier, never()).notifySharedNote(any(), any());
    }
}
