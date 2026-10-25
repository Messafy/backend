package com.luispiquinrey.backend.account.application.use_cases;
import com.luispiquinrey.backend.share.logging.Logger;

import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.application.dto.request.GetAccountRequest;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountUseCaseTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";
    private static final String EMAIL = "user@example.com";
    private static final String ENCODED_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final String ENCODED_LOCKER = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e";
    private static final String PUBLIC_CODE = "V1StGXR8_Z5j";

    @Mock
    private AccountRepository repository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private Logger log;

    @InjectMocks
    private GetAccountUseCase service;

    @Test
    @Timeout(1)
    @Tag("findAccountByIdFound")
    void shouldReturnAccountWhenFoundById() {
        Account account = account();
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.of(account));

        var result = service.findById(new GetAccountRequest(ACCOUNT_ID));
        assertTrue(result.isSuccess());
        AccountResponse view = result.getValue();

        assertViewOf(view, account);
        verify(repository).findById(new UserId(ACCOUNT_ID));
    }

    @Test
    @Timeout(1)
    @Tag("findAccountByIdMissing")
    void shouldReturnNotFoundWhenAccountIsMissingById() {
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.empty());

        var result = service.findById(new GetAccountRequest(ACCOUNT_ID));
        assertTrue(result.isFailure());
        assertEquals(ApplicationError.notFoundError("account not found: " + ACCOUNT_ID), result.getError());

        verify(repository).findById(new UserId(ACCOUNT_ID));
    }

    @Test
    @Timeout(1)
    @Tag("getMyAccountFound")
    void shouldReturnCurrentAccountWhenFound() {
        UserId accountId = new UserId(ACCOUNT_ID);
        Account account = account();
        when(repository.findById(accountId)).thenReturn(Optional.of(account));

        var result = service.getMyAccount(new GetAccountRequest(accountId.id()));
        assertTrue(result.isSuccess());
        AccountResponse view = result.getValue();

        assertViewOf(view, account);
        verify(repository).findById(accountId);
    }

    @Test
    @Timeout(1)
    @Tag("getMyAccountMissing")
    void shouldReturnNotFoundWhenCurrentAccountIsMissing() {
        UserId accountId = new UserId(ACCOUNT_ID);
        when(repository.findById(accountId)).thenReturn(Optional.empty());

        var result = service.getMyAccount(new GetAccountRequest(accountId.id()));
        assertTrue(result.isFailure());
        assertEquals(ApplicationError.notFoundError("account not found: " + ACCOUNT_ID), result.getError());

        verify(repository).findById(accountId);
    }

    @Test
    @Timeout(1)
    @Tag("accountLookupActive")
    void shouldConfirmActiveAccountExists() {
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.of(account()));

        assertTrue(service.existsActiveAccount(ACCOUNT_ID));
        verify(repository).findById(new UserId(ACCOUNT_ID));
    }

    @Test
    @Timeout(1)
    @Tag("accountLookupDeleted")
    void shouldRejectDeletedAccount() {
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.of(deletedAccount()));

        assertFalse(service.existsActiveAccount(ACCOUNT_ID));
        verify(repository).findById(new UserId(ACCOUNT_ID));
    }

    @Test
    @Timeout(1)
    @Tag("accountLookupMissing")
    void shouldRejectMissingAccount() {
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.empty());

        assertFalse(service.existsActiveAccount(ACCOUNT_ID));
        verify(repository).findById(new UserId(ACCOUNT_ID));
    }

    @Test
    @Timeout(1)
    @Tag("accountLookupPublicCode")
    void shouldFindActiveAccountIdByPublicCode() {
        when(repository.findByPublicCode(PUBLIC_CODE)).thenReturn(Optional.of(account()));

        Optional<UserId> result = service.findActiveAccountIdByPublicCode(PUBLIC_CODE);

        assertTrue(result.isPresent());
        assertEquals(ACCOUNT_ID, result.get().id());
        verify(repository).findByPublicCode(PUBLIC_CODE);
    }

    @Test
    @Timeout(1)
    @Tag("accountLookupPublicCode")
    void shouldRejectMissingAccountByPublicCode() {
        when(repository.findByPublicCode(PUBLIC_CODE)).thenReturn(Optional.empty());

        Optional<UserId> result = service.findActiveAccountIdByPublicCode(PUBLIC_CODE);

        assertFalse(result.isPresent());
        verify(repository).findByPublicCode(PUBLIC_CODE);
    }

    @Test
    @Timeout(1)
    @Tag("accountLookupPublicCode")
    void shouldRejectDeletedAccountByPublicCode() {
        when(repository.findByPublicCode(PUBLIC_CODE)).thenReturn(Optional.of(deletedAccount()));

        Optional<UserId> result = service.findActiveAccountIdByPublicCode(PUBLIC_CODE);

        assertFalse(result.isPresent());
        verify(repository).findByPublicCode(PUBLIC_CODE);
    }

    @Test
    @Timeout(1)
    @Tag("matchesLockerPassword")
    void shouldMatchLockerPassword() {
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.of(account()));
        when(passwordHasher.matches("raw-locker-password", new EncodedPassword(ENCODED_LOCKER)))
                .thenReturn(true);

        boolean matches = service.matchesLockerPassword(new UserId(ACCOUNT_ID), "raw-locker-password");

        assertTrue(matches);
        verify(repository).findById(new UserId(ACCOUNT_ID));
        verify(passwordHasher).matches("raw-locker-password", new EncodedPassword(ENCODED_LOCKER));
    }

    @Test
    @Timeout(1)
    @Tag("matchesLockerPassword")
    void shouldNotMatchLockerPasswordForMissingAccount() {
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.empty());

        boolean matches = service.matchesLockerPassword(new UserId(ACCOUNT_ID), "raw-locker-password");

        assertFalse(matches);
        verify(repository).findById(new UserId(ACCOUNT_ID));
    }

    @Test
    @Timeout(1)
    @Tag("getAccountUseCase")
    void shouldRejectNullRequest() {
        assertEquals(ApplicationError.validationError("get account request cannot be null"), service.findById(null).getError());
        assertEquals(ApplicationError.validationError("get account request cannot be null"), service.getMyAccount(null).getError());
        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    private void assertViewOf(AccountResponse view, Account account) {
        assertEquals(account.id().id(), view.id());
        assertEquals(account.email().email(), view.email());
        assertEquals(account.publicCode().value(), view.publicCode());
        assertEquals(account.role().name(), view.role());
        assertEquals(account.status().name(), view.status());
        assertEquals(account.isVerified(), view.verified());
        assertEquals(account.createdAt().value(), view.createdAt());
        assertEquals(account.lastLoginAt() == null ? null : account.lastLoginAt().value(), view.lastLoginAt());
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword(ENCODED_PASSWORD))
                .publicCode(new PublicCode(PUBLIC_CODE))
                .lockerPassword(new LockerPassword(ENCODED_LOCKER))
                .createdAt(new Date(Instant.parse("2026-01-01T10:00:00Z")))
                .lastLoginAt(new Date(Instant.parse("2026-01-02T10:00:00Z")))
                .build();
    }

    private Account deletedAccount() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword(ENCODED_PASSWORD))
                .publicCode(new PublicCode(PUBLIC_CODE))
                .lockerPassword(new LockerPassword(ENCODED_LOCKER))
                .status(AccountStatus.DELETED)
                .build();
    }
}
