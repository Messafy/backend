package com.luispiquinrey.backend.account.slices.get;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountServiceTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";
    private static final String EMAIL = "user@example.com";

    @Mock
    private GetAccountRepository repository;

    @InjectMocks
    private GetAccountService service;

    @Test
    @Timeout(1)
    @Tag("findAccountByIdFound")
    void shouldReturnAccountWhenFoundById() {
        Account account = account();
        when(repository.findById(new UserId(ACCOUNT_ID))).thenReturn(Optional.of(account));

        Account result = service.findById(ACCOUNT_ID);

        assertSame(account, result);
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

        Account result = service.getMyAccount(accountId);

        assertSame(account, result);
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
    @Tag("findAccountByEmailFound")
    void shouldReturnAccountWhenFoundByEmail() {
        Account account = account();
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(account));

        Account result = service.findByEmail(EMAIL);

        assertSame(account, result);
        verify(repository).findByEmail(EMAIL);
    }

    @Test
    @Timeout(1)
    @Tag("findAccountByEmailMissing")
    void shouldThrowWhenAccountIsMissingByEmail() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> service.findByEmail(EMAIL));

        verify(repository).findByEmail(EMAIL);
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .build();
    }
}
