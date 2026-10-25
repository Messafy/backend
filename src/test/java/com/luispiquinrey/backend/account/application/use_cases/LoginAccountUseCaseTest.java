package com.luispiquinrey.backend.account.application.use_cases;
import com.luispiquinrey.backend.share.logging.Logger;

import com.luispiquinrey.backend.account.application.dto.request.LoginAccountRequest;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.application.port.TokenIssuer;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAccountUseCaseTest {

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenIssuer tokenIssuer;

    @Mock
    private AccountRepository repository;

    @Mock
    private Logger log;

    @InjectMocks
    private LoginAccountUseCase service;

    @Test
    @Timeout(1)
    @Tag("loginAccountUseCase")
    void shouldLoginAccountAndReturnIssuedToken() {
        Account account = account();
        LoginAccountRequest request = new LoginAccountRequest("USER@EXAMPLE.COM", "correct-password");
        Instant beforeLogin = Instant.now();
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("correct-password", account.encodedPassword())).thenReturn(true);
        when(tokenIssuer.issue(account)).thenReturn("opaque-login-token");

        var response = service.login(request);

        assertTrue(response.isSuccess());
        assertEquals("opaque-login-token", response.getValue().token());
        assertNotNull(account.lastLoginAt());
        assertFalse(account.lastLoginAt().value().isBefore(beforeLogin));
        verify(repository).save(account);

        verify(tokenIssuer).issue(account);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountUseCase")
    void shouldRejectNullRequest() {
        assertEquals(ApplicationError.validationError("login request cannot be null"), service.login(null).getError());

        verifyNoInteractions(passwordHasher, tokenIssuer, repository);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountUseCase")
    void shouldRejectBlankPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoginAccountRequest("user@example.com", " \t"));

        verifyNoInteractions(passwordHasher, tokenIssuer, repository);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountUseCase")
    void shouldRejectMissingAccount() {
        LoginAccountRequest request = new LoginAccountRequest("user@example.com", "correct-password");
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        var result = service.login(request);
        assertTrue(result.isFailure());
        assertEquals(ApplicationError.unauthorizedError("invalid credentials"), result.getError());

        verify(repository, never()).save(any());
        verifyNoInteractions(passwordHasher, tokenIssuer);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountUseCase")
    void shouldRejectIncorrectPasswordWithoutUpdatingAccount() {
        Account account = account();
        LoginAccountRequest request = new LoginAccountRequest("user@example.com", "incorrect-password");
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("incorrect-password", account.encodedPassword())).thenReturn(false);

        var result = service.login(request);
        assertTrue(result.isFailure());
        assertEquals(ApplicationError.unauthorizedError("invalid credentials"), result.getError());

        verify(repository, never()).save(any());
        verifyNoInteractions(tokenIssuer);
    }

    private Account account() {
        Account account = new Account.AccountBuilder()
                .id(new UserId("507f1f77bcf86cd799439011"))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword(
                        "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
                ))
                .publicCode(new PublicCode("V1StGXR8_Z5j"))
                .lockerPassword(new LockerPassword(
                        "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e"
                ))
                .build();
        account.verify();
        return account;
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountUseCase")
    void shouldTranslateDomainConflictWithoutSavingOrIssuingToken() {
        Account account = account();
        account.delete();
        var request = new LoginAccountRequest("user@example.com", "correct-password");
        when(repository.findByEmail(request.email())).thenReturn(Optional.of(account));
        when(passwordHasher.matches(request.rawPassword(), account.encodedPassword())).thenReturn(true);

        var result = service.login(request);

        assertTrue(result.isFailure());
        assertEquals(com.luispiquinrey.backend.share.result.ErrorType.CONFLICT_ERROR, result.getError().type());
        verify(repository, never()).save(any());
        verifyNoInteractions(tokenIssuer);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountUseCase")
    void shouldNotDisguiseUnexpectedPersistenceFailureAsInvalidCredentials() {
        var request = new LoginAccountRequest("user@example.com", "password");
        var failure = new IllegalStateException("database unavailable");
        when(repository.findByEmail(request.email())).thenThrow(failure);

        org.junit.jupiter.api.Assertions.assertSame(failure,
                assertThrows(IllegalStateException.class, () -> service.login(request)));
        verifyNoInteractions(passwordHasher, tokenIssuer);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountUseCase")
    void shouldTranslateConcurrentSaveConflictWithoutIssuingToken() {
        Account account = account();
        var request = new LoginAccountRequest("user@example.com", "correct-password");
        when(repository.findByEmail(request.email())).thenReturn(Optional.of(account));
        when(passwordHasher.matches(request.rawPassword(), account.encodedPassword())).thenReturn(true);
        org.mockito.Mockito.doThrow(new com.luispiquinrey.backend.account.domain.exception.AccountConflictException(
                "account was modified concurrently; reload and retry")).when(repository).save(account);

        var result = service.login(request);

        assertTrue(result.isFailure());
        assertEquals(ApplicationError.conflictError("account was modified concurrently; reload and retry"), result.getError());
        verifyNoInteractions(tokenIssuer);
    }
}
