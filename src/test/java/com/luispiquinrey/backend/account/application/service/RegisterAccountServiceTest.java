package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.dto.RegisterAccountCommand;
import com.luispiquinrey.backend.account.application.port.AccountIdGenerator;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.Role;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.events.DomainEventPublisher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterAccountServiceTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private AccountIdGenerator accountIdGenerator;

    @InjectMocks
    private RegisterAccountService service;

    @Test
    @Timeout(1)
    @Tag("registerAccountService")
    void shouldRegisterAccountFromCommand() {
        RegisterAccountCommand command = new RegisterAccountCommand(
                "user@example.com",
                "Password1!"
        );
        EncodedPassword encodedPassword = new EncodedPassword("encoded-password");
        when(passwordHasher.hash("Password1!")).thenReturn(encodedPassword);
        when(accountIdGenerator.nextId()).thenReturn("507f1f77bcf86cd799439011");

        AccountView view = service.register(command);

        verify(passwordHasher).hash("Password1!");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository).save(captor.capture());
        verify(domainEventPublisher).publish(new AccountRegistered(accountIdGenerator.nextId(), "user@example.com"));

        Account account = captor.getValue();
        assertNotNull(account.id());
        assertEquals("user@example.com", account.email().email());
        assertTrue(account.encodedPassword() == encodedPassword || account.encodedPassword().equals(encodedPassword));
        assertEquals(Role.USER, account.role());
        assertEquals(AccountStatus.ACTIVE, account.status());
        assertFalse(account.isVerified());
        assertNotNull(account.createdAt());
        assertNull(account.lastLoginAt());

        assertEquals(account.id().id(), view.id());
        assertEquals("user@example.com", view.email());
        assertEquals("USER", view.role());
        assertEquals("ACTIVE", view.status());
        assertFalse(view.verified());
        assertEquals(account.createdAt().value(), view.createdAt());
        assertNull(view.lastLoginAt());
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountService")
    void shouldThrowWhenCommandIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null));

        verifyNoInteractions(passwordHasher, repository);
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountService")
    void shouldThrowWhenPasswordIsBlank() {
        RegisterAccountCommand command = new RegisterAccountCommand(
                "user@example.com",
                "   "
        );

        assertThrows(IllegalArgumentException.class, () -> service.register(command));

        verifyNoInteractions(passwordHasher, repository);
    }
}
