package com.luispiquinrey.backend.account.slices.register;

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
        AccountRegisterRequest request = new AccountRegisterRequest(
                "user@example.com",
                "Password1!"
        );
        AccountRegisterResponse body = new AccountRegisterResponse(
                "507f1f77bcf86cd799439011",
                "user@example.com",
                "USER",
                Instant.parse("2026-09-12T10:15:30Z")
        );
        RegisterAccountService service = mock(RegisterAccountService.class);
        when(service.register(request)).thenReturn(body);
        RegisterAccountController controller = new RegisterAccountController(service);

        ResponseEntity<AccountRegisterResponse> response = controller.register(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(body, response.getBody());
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldPropagateExceptionFromService() {
        AccountRegisterRequest request = new AccountRegisterRequest(
                "user@example.com",
                "   "
        );
        RegisterAccountService service = mock(RegisterAccountService.class);
        IllegalArgumentException exception = new IllegalArgumentException("raw password cannot be blank");
        when(service.register(request)).thenThrow(exception);
        RegisterAccountController controller = new RegisterAccountController(service);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> controller.register(request)
        );

        assertSame(exception, thrown);
    }
}
