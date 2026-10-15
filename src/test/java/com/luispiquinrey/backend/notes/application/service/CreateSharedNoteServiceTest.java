package com.luispiquinrey.backend.notes.application.service;
import com.luispiquinrey.backend.share.logging.Logger;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.dto.CreateSharedNoteCommand;
import com.luispiquinrey.backend.notes.application.port.NoteIdGenerator;
import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.exception.NoteValidationException;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSharedNoteServiceTest {

    private static final String RECIPIENT_PUBLIC_CODE = "V1StGXR8_Z5j";
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
    private CreateSharedNoteService service;

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createSharedNoteService")
    void shouldCreateSharedNoteWhenPublicCodeAndLockerPasswordAreValid() {
        when(accountLookup.findActiveAccountIdByPublicCode(RECIPIENT_PUBLIC_CODE))
                .thenReturn(Optional.of(RECIPIENT_ID));
        when(accountLookup.matchesLockerPassword(eq(RECIPIENT_ID), eq("CorrectLocker!")))
                .thenReturn(true);
        when(idGenerator.nextId()).thenReturn("507f1f77bcf86cd799439011");

        CreateSharedNoteCommand command = new CreateSharedNoteCommand(
                "Hello",
                "Body",
                RECIPIENT_PUBLIC_CODE,
                "CorrectLocker!",
                null
        );

        Note note = service.create(command, "507f1f77bcf86cd799439012");

        assertInstanceOf(SharedNote.class, note);
        verify(accountLookup).findActiveAccountIdByPublicCode(RECIPIENT_PUBLIC_CODE);
        verify(accountLookup).matchesLockerPassword(RECIPIENT_ID, "CorrectLocker!");
        verify(repository).save(note);
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createSharedNoteService")
    void shouldRejectWhenLockerPasswordDoesNotMatch() {
        when(accountLookup.findActiveAccountIdByPublicCode(RECIPIENT_PUBLIC_CODE))
                .thenReturn(Optional.of(RECIPIENT_ID));
        when(accountLookup.matchesLockerPassword(eq(RECIPIENT_ID), eq("WrongLocker!")))
                .thenReturn(false);

        CreateSharedNoteCommand command = new CreateSharedNoteCommand(
                "Hello",
                "Body",
                RECIPIENT_PUBLIC_CODE,
                "WrongLocker!",
                null
        );

        assertThrows(
                NoteValidationException.class,
                () -> service.create(command, "507f1f77bcf86cd799439012")
        );
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createSharedNoteService")
    void shouldRejectWhenPublicCodeDoesNotResolve() {
        when(accountLookup.findActiveAccountIdByPublicCode("MissingCode00"))
                .thenReturn(Optional.empty());

        CreateSharedNoteCommand command = new CreateSharedNoteCommand(
                "Hello",
                "Body",
                "MissingCode00",
                "AnyLocker1!",
                null
        );

        assertThrows(
                NoteValidationException.class,
                () -> service.create(command, "507f1f77bcf86cd799439012")
        );
        verify(accountLookup, never()).matchesLockerPassword(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createSharedNoteService")
    void shouldRejectWhenRecipientPublicCodeIsBlank() {
        CreateSharedNoteCommand command = new CreateSharedNoteCommand(
                "Hello",
                "Body",
                " ",
                "AnyLocker1!",
                null
        );

        assertThrows(
                NoteValidationException.class,
                () -> service.create(command, "507f1f77bcf86cd799439012")
        );
        verify(accountLookup, never()).findActiveAccountIdByPublicCode(any());
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("createSharedNoteService")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(
                NoteValidationException.class,
                () -> service.create(null, "507f1f77bcf86cd799439012")
        );
    }
}
