package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.domain.exception.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountServiceTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";
    private static final String EMAIL = "user@example.com";

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private GetAccountService service;

    @Test
    @Timeout(1)
    @Tag("findAccountByIdFound")
    void shouldReturnAccountWhenFoundById() {
        Account account = account();
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.of(account));

        AccountView view = service.findById(ACCOUNT_ID);

        assertViewOf(view, account);
        verify(repository).findById(new UserId(ACCOUNT_ID));
    }

    @Test
    @Timeout(1)
    @Tag("findAccountByIdMissing")
    void shouldThrowWhenAccountIsMissingById() {
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> service.findById(ACCOUNT_ID));

        verify(repository).findById(new UserId(ACCOUNT_ID));
    }

    @Test
    @Timeout(1)
    @Tag("getMyAccountFound")
    void shouldReturnCurrentAccountWhenFound() {
        UserId accountId = new UserId(ACCOUNT_ID);
        Account account = account();
        when(repository.findById(accountId)).thenReturn(Optional.of(account));

        AccountView view = service.getMyAccount(accountId);

        assertViewOf(view, account);
        verify(repository).findById(accountId);
    }

    @Test
    @Timeout(1)
    @Tag("getMyAccountMissing")
    void shouldThrowWhenCurrentAccountIsMissing() {
        UserId accountId = new UserId(ACCOUNT_ID);
        when(repository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> service.getMyAccount(accountId));

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

    private void assertViewOf(AccountView view, Account account) {
        assertEquals(account.id().id(), view.id());
        assertEquals(account.email().email(), view.email());
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
                .encodedPassword(new EncodedPassword("encoded-password"))
                .createdAt(new Date(Instant.parse("2026-01-01T10:00:00Z")))
                .lastLoginAt(new Date(Instant.parse("2026-01-02T10:00:00Z")))
                .build();
    }

    private Account deletedAccount() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .status(AccountStatus.DELETED)
                .build();
    }
}
