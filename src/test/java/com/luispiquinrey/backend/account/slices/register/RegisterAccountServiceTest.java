package com.luispiquinrey.backend.account.slices.register;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.AccountStatus;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.account.domain.Role;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterAccountServiceTest {

    @Mock
    private RegisterAccountRepository repository;

    @Mock
    private RegisterPasswordHasher passwordHasher;

    @InjectMocks
    private RegisterAccountService service;

    @Test
    @Timeout(1)
    @Tag("registerAccountService")
    void shouldRegisterAccountFromRequest() {
        AccountRegisterRequest request = new AccountRegisterRequest(
                "user@example.com",
                "Password1!"
        );
        EncodedPassword encodedPassword = new EncodedPassword("encoded-password");
        when(passwordHasher.hash("Password1!")).thenReturn(encodedPassword);

        AccountRegisterResponse response = service.register(request);

        verify(passwordHasher).hash("Password1!");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository).save(captor.capture());

        Account account = captor.getValue();
        assertNotNull(account.id());
        assertEquals("user@example.com", account.email().email());
        assertSame(encodedPassword, account.encodedPassword());
        assertEquals(Role.USER, account.role());
        assertEquals(AccountStatus.ACTIVE, account.status());
        assertFalse(account.isVerified());
        assertNotNull(account.createdAt());
        assertNull(account.lastLoginAt());

        assertEquals(account.id().id(), response.id());
        assertEquals("user@example.com", response.email());
        assertEquals("USER", response.role());
        assertEquals(account.createdAt().value(), response.createdAt());
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountService")
    void shouldThrowWhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null));

        verifyNoInteractions(passwordHasher, repository);
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountService")
    void shouldThrowWhenPasswordIsBlank() {
        AccountRegisterRequest request = new AccountRegisterRequest(
                "user@example.com",
                "   "
        );

        assertThrows(IllegalArgumentException.class, () -> service.register(request));

        verifyNoInteractions(passwordHasher, repository);
    }
}
