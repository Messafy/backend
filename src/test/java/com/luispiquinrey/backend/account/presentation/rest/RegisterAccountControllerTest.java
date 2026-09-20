package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.dto.RegisterAccountCommand;
import com.luispiquinrey.backend.account.application.service.RegisterAccountService;
import com.luispiquinrey.backend.account.presentation.dto.RegisterAccountResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterAccountControllerTest {

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldReturnCreatedWithRegistrationResponse() {
        RegisterAccountCommand command = new RegisterAccountCommand(
                "user@example.com",
                "Password1!"
        );
        AccountView view = new AccountView(
                "507f1f77bcf86cd799439011",
                "user@example.com",
                "USER",
                "ACTIVE",
                false,
                Instant.parse("2026-09-12T10:15:30Z"),
                null
        );
        RegisterAccountService service = mock(RegisterAccountService.class);
        when(service.register(command)).thenReturn(view);
        RegisterAccountController controller = new RegisterAccountController(service);

        ResponseEntity<RegisterAccountResponse> response = controller.register(command);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("507f1f77bcf86cd799439011", response.getBody().id());
        assertEquals("user@example.com", response.getBody().email());
        assertEquals("USER", response.getBody().role());
        assertEquals(Instant.parse("2026-09-12T10:15:30Z"), response.getBody().createdAt());
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldPropagateExceptionFromService() {
        RegisterAccountCommand command = new RegisterAccountCommand(
                "user@example.com",
                "   "
        );
        RegisterAccountService service = mock(RegisterAccountService.class);
        IllegalArgumentException exception = new IllegalArgumentException("raw password cannot be blank");
        when(service.register(command)).thenThrow(exception);
        RegisterAccountController controller = new RegisterAccountController(service);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> controller.register(command)
        );

        assertSame(exception, thrown);
    }
}
