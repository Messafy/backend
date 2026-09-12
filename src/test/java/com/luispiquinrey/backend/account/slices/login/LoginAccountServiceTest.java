package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.AccountPasswordMismatchException;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.account.infrastructure.AccountUserDetails;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAccountServiceTest {

    @Mock
    private LoginPasswordHasher passwordHasher;

    @Mock
    private LoginTokenIssuer tokenIssuer;

    @Mock
    private LoginAccountRepository repository;

    @InjectMocks
    private LoginAccountService service;

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldLoginAccountAndReturnIssuedToken() {
        Account account = account();
        AccountLoginRequest request = new AccountLoginRequest("USER@EXAMPLE.COM", "correct-password");
        Instant beforeLogin = Instant.now();
        when(repository.byEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("correct-password", account.encodedPassword())).thenReturn(true);
        when(tokenIssuer.issue(any(AccountUserDetails.class))).thenReturn("opaque-login-token");

        AccountLoginResponse response = service.login(request);

        assertEquals("opaque-login-token", response.token());
        assertNotNull(account.lastLoginAt());
        assertFalse(account.lastLoginAt().value().isBefore(beforeLogin));
        verify(repository).save(account);

        ArgumentCaptor<AccountUserDetails> userDetailsCaptor = ArgumentCaptor.forClass(AccountUserDetails.class);
        verify(tokenIssuer).issue(userDetailsCaptor.capture());
        assertSame(account, userDetailsCaptor.getValue().account());
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldRejectNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.login(null));

        verifyNoInteractions(passwordHasher, tokenIssuer, repository);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldRejectBlankPassword() {
        AccountLoginRequest request = new AccountLoginRequest("user@example.com", " \t");

        assertThrows(IllegalArgumentException.class, () -> service.login(request));

        verifyNoInteractions(passwordHasher, tokenIssuer, repository);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldRejectMissingAccount() {
        AccountLoginRequest request = new AccountLoginRequest("user@example.com", "correct-password");
        when(repository.byEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.login(request));

        verify(repository, never()).save(any());
        verifyNoInteractions(passwordHasher, tokenIssuer);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldRejectIncorrectPasswordWithoutUpdatingAccount() {
        Account account = account();
        AccountLoginRequest request = new AccountLoginRequest("user@example.com", "incorrect-password");
        when(repository.byEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("incorrect-password", account.encodedPassword())).thenReturn(false);

        assertThrows(AccountPasswordMismatchException.class, () -> service.login(request));

        verify(repository, never()).save(any());
        verifyNoInteractions(tokenIssuer);
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId("507f1f77bcf86cd799439011"))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .build();
    }
}
