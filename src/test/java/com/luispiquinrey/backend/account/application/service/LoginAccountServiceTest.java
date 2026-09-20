package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.application.dto.LoginAccountCommand;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.application.port.TokenIssuer;
import com.luispiquinrey.backend.account.domain.exception.AccountAuthenticationException;
import com.luispiquinrey.backend.account.domain.exception.AccountPasswordMismatchException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAccountServiceTest {

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenIssuer tokenIssuer;

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private LoginAccountService service;

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldLoginAccountAndReturnIssuedToken() {
        Account account = account();
        LoginAccountCommand command = new LoginAccountCommand("USER@EXAMPLE.COM", "correct-password");
        Instant beforeLogin = Instant.now();
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("correct-password", account.encodedPassword())).thenReturn(true);
        when(tokenIssuer.issue(account)).thenReturn("opaque-login-token");

        String token = service.login(command);

        assertEquals("opaque-login-token", token);
        assertNotNull(account.lastLoginAt());
        assertFalse(account.lastLoginAt().value().isBefore(beforeLogin));
        verify(repository).save(account);

        verify(tokenIssuer).issue(account);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldRejectNullCommand() {
        assertThrows(IllegalArgumentException.class, () -> service.login(null));

        verifyNoInteractions(passwordHasher, tokenIssuer, repository);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldRejectBlankPassword() {
        LoginAccountCommand command = new LoginAccountCommand("user@example.com", " \t");

        assertThrows(IllegalArgumentException.class, () -> service.login(command));

        verifyNoInteractions(passwordHasher, tokenIssuer, repository);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldRejectMissingAccount() {
        LoginAccountCommand command = new LoginAccountCommand("user@example.com", "correct-password");
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(AccountAuthenticationException.class, () -> service.login(command));

        verify(repository, never()).save(any());
        verifyNoInteractions(passwordHasher, tokenIssuer);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountService")
    void shouldRejectIncorrectPasswordWithoutUpdatingAccount() {
        Account account = account();
        LoginAccountCommand command = new LoginAccountCommand("user@example.com", "incorrect-password");
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("incorrect-password", account.encodedPassword())).thenReturn(false);

        assertThrows(AccountPasswordMismatchException.class, () -> service.login(command));

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
