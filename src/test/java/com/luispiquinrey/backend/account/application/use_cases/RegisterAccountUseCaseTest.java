package com.luispiquinrey.backend.account.application.use_cases;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ApplicationError;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.application.dto.request.RegisterAccountRequest;
import com.luispiquinrey.backend.account.application.port.AccountIdGenerator;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.application.port.PublicCodeGenerator;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
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
class RegisterAccountUseCaseTest {

    private static final String ENCODED_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final String ENCODED_LOCKER = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e";
    private static final String PUBLIC_CODE = "V1StGXR8_Z5j";

    @Mock
    private AccountRepository repository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private AccountIdGenerator accountIdGenerator;

    @Mock
    private PublicCodeGenerator publicCodeGenerator;

    @Mock
    private Logger log;

    @InjectMocks
    private RegisterAccountUseCase service;

    @Test
    @Timeout(1)
    @Tag("registerAccountUseCase")
    void shouldRegisterAccountFromRequest() {
        RegisterAccountRequest request = new RegisterAccountRequest(
                "user@example.com",
                "Password1!",
                "Locker1!"
        );
        EncodedPassword encodedPassword = new EncodedPassword(ENCODED_PASSWORD);
        EncodedPassword encodedLocker = new EncodedPassword(ENCODED_LOCKER);
        when(passwordHasher.hash("Password1!")).thenReturn(encodedPassword);
        when(passwordHasher.hash("Locker1!")).thenReturn(encodedLocker);
        when(accountIdGenerator.nextId()).thenReturn("507f1f77bcf86cd799439011");
        when(publicCodeGenerator.nextCode()).thenReturn(PUBLIC_CODE);

        var result = service.register(request);
        assertTrue(result.isSuccess());
        AccountResponse view = result.getValue();

        verify(passwordHasher).hash("Password1!");
        verify(passwordHasher).hash("Locker1!");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository).save(captor.capture());
        verify(domainEventPublisher).publish(new AccountRegistered(accountIdGenerator.nextId(), "user@example.com"));

        Account account = captor.getValue();
        assertNotNull(account.id());
        assertEquals("user@example.com", account.email().email());
        assertTrue(account.encodedPassword() == encodedPassword || account.encodedPassword().equals(encodedPassword));
        assertEquals(new PublicCode(PUBLIC_CODE), account.publicCode());
        assertEquals(new LockerPassword(ENCODED_LOCKER), account.lockerPassword());
        assertEquals(Role.USER, account.role());
        assertEquals(AccountStatus.ACTIVE, account.status());
        assertFalse(account.isVerified());
        assertNotNull(account.createdAt());
        assertNull(account.lastLoginAt());

        assertEquals(account.id().id(), view.id());
        assertEquals("user@example.com", view.email());
        assertEquals(PUBLIC_CODE, view.publicCode());
        assertEquals("USER", view.role());
        assertEquals("ACTIVE", view.status());
        assertFalse(view.verified());
        assertEquals(account.createdAt().value(), view.createdAt());
        assertNull(view.lastLoginAt());
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountUseCase")
    void shouldReturnValidationFailureWhenRequestIsNull() {
        assertEquals(ApplicationError.validationError("register request cannot be null"), service.register(null).getError());

        verifyNoInteractions(passwordHasher, repository);
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountUseCase")
    void shouldThrowWhenPasswordIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new RegisterAccountRequest(
                "user@example.com",
                "   ",
                "Locker1!"
        ));

        verifyNoInteractions(passwordHasher, repository);
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountUseCase")
    void shouldThrowWhenLockerPasswordIsTooShort() {
        assertThrows(IllegalArgumentException.class, () -> new RegisterAccountRequest(
                "user@example.com",
                "Password1!",
                "short"
        ));

        verifyNoInteractions(passwordHasher, repository);
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountUseCase")
    void shouldTranslateDuplicateAccountWithoutPublishingEvent() {
        var request = new RegisterAccountRequest("user@example.com", "Password1!", "Locker1!");
        when(passwordHasher.hash(request.rawPassword())).thenReturn(new EncodedPassword(ENCODED_PASSWORD));
        when(passwordHasher.hash(request.rawLockerPassword())).thenReturn(new EncodedPassword(ENCODED_LOCKER));
        when(accountIdGenerator.nextId()).thenReturn("opaque-id");
        when(publicCodeGenerator.nextCode()).thenReturn(PUBLIC_CODE);
        org.mockito.Mockito.doThrow(new com.luispiquinrey.backend.account.domain.exception.AccountConflictException(
                "email already registered")).when(repository).save(org.mockito.Mockito.any(Account.class));

        var result = service.register(request);

        assertTrue(result.isFailure());
        assertEquals(ApplicationError.conflictError("email already registered"), result.getError());
        verifyNoInteractions(domainEventPublisher);
    }
}
