package com.luispiquinrey.backend.account.application.use_cases;
import com.luispiquinrey.backend.share.logging.Logger;

import com.luispiquinrey.backend.account.application.port.VerificationTokenCodec;
import com.luispiquinrey.backend.account.application.dto.request.VerifyAccountRequest;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyAccountUseCaseTest {

    @Mock
    private VerificationTokenCodec verificationTokenCodec;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Logger log;

    @InjectMocks
    private VerifyAccountUseCase service;

    @Test
    @Timeout(1)
    @Tag("verifyAccountUseCase")
    void shouldIssueTokenThroughCodec() {
        when(verificationTokenCodec.issue("507f1f77bcf86cd799439011")).thenReturn("raw-token");

        String token = service.issue("507f1f77bcf86cd799439011");

        assertEquals("raw-token", token);
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountUseCase")
    void shouldRejectNullRequestBeforeInvokingAdapters() {
        assertEquals(ApplicationError.validationError("verify account request cannot be null"), service.verifyByToken(null).getError());
        org.mockito.Mockito.verifyNoInteractions(verificationTokenCodec, accountRepository);
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountUseCase")
    void shouldVerifyAndSaveAccountForValidToken() {
        Account account = org.mockito.Mockito.mock(Account.class);
        when(account.isVerified()).thenReturn(true);
        when(verificationTokenCodec.extractAccountId("raw-token")).thenReturn("507f1f77bcf86cd799439011");
        when(accountRepository.findById(new UserId("507f1f77bcf86cd799439011"))).thenReturn(Optional.of(account));

        var response = service.verifyByToken(new VerifyAccountRequest("raw-token"));

        assertTrue(response.isSuccess());
        assertTrue(response.getValue().verified());

        verify(account).verify();
        verify(accountRepository).save(account);
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountUseCase")
    void shouldReturnNotFoundWhenTokenAccountIsMissing() {
        when(verificationTokenCodec.extractAccountId("raw-token")).thenReturn("507f1f77bcf86cd799439099");
        when(accountRepository.findById(new UserId("507f1f77bcf86cd799439099"))).thenReturn(Optional.empty());

        var result = service.verifyByToken(new VerifyAccountRequest("raw-token"));
        assertTrue(result.isFailure());
        assertEquals(ApplicationError.notFoundError("account not found: 507f1f77bcf86cd799439099"), result.getError());

        verify(accountRepository, never()).save(org.mockito.Mockito.any());
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountUseCase")
    void shouldTranslateInvalidTokenFailure() {
        when(verificationTokenCodec.extractAccountId("broken"))
                .thenThrow(new com.luispiquinrey.backend.account.domain.exception.AccountVerificationTokenException("invalid"));

        var result = service.verifyByToken(new VerifyAccountRequest("broken"));
        assertTrue(result.isFailure());
        assertEquals(ApplicationError.validationError("invalid verification token"), result.getError());
        org.mockito.Mockito.verifyNoInteractions(accountRepository);
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountUseCase")
    void shouldTranslateDomainConflictWithoutSaving() {
        Account account = org.mockito.Mockito.mock(Account.class);
        when(verificationTokenCodec.extractAccountId("raw-token")).thenReturn("opaque-id");
        when(accountRepository.findById(new UserId("opaque-id"))).thenReturn(Optional.of(account));
        org.mockito.Mockito.doThrow(new com.luispiquinrey.backend.account.domain.exception.AccountConflictException(
                "account cannot be verified")).when(account).verify();

        var result = service.verifyByToken(new VerifyAccountRequest("raw-token"));

        assertTrue(result.isFailure());
        assertEquals(ApplicationError.conflictError("account cannot be verified"), result.getError());
        verify(accountRepository, never()).save(org.mockito.Mockito.any());
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountUseCase")
    void shouldTranslateConcurrentSaveConflict() {
        Account account = org.mockito.Mockito.mock(Account.class);
        when(verificationTokenCodec.extractAccountId("raw-token")).thenReturn("opaque-id");
        when(accountRepository.findById(new UserId("opaque-id"))).thenReturn(Optional.of(account));
        org.mockito.Mockito.doThrow(new com.luispiquinrey.backend.account.domain.exception.AccountConflictException(
                "account was modified concurrently; reload and retry")).when(accountRepository).save(account);

        var result = service.verifyByToken(new VerifyAccountRequest("raw-token"));

        assertTrue(result.isFailure());
        assertEquals(ApplicationError.conflictError("account was modified concurrently; reload and retry"), result.getError());
    }
}
