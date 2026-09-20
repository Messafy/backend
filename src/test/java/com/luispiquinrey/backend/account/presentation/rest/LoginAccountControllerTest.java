package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.LoginAccountCommand;
import com.luispiquinrey.backend.account.application.service.LoginAccountService;
import com.luispiquinrey.backend.account.presentation.dto.LoginAccountResponse;
import com.luispiquinrey.backend.account.domain.exception.AccountPasswordMismatchException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginAccountControllerTest {

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldReturnOkWithLoginResponse() {
        LoginAccountService service = mock(LoginAccountService.class);
        LoginAccountController controller = new LoginAccountController(service);
        LoginAccountCommand command = new LoginAccountCommand("user@example.com", "correct-password");
        when(service.login(command)).thenReturn("opaque-login-token");

        ResponseEntity<LoginAccountResponse> response = controller.login(command);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("opaque-login-token", response.getBody().token());
        verify(service).login(command);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldPropagateLoginFailure() {
        LoginAccountService service = mock(LoginAccountService.class);
        LoginAccountController controller = new LoginAccountController(service);
        LoginAccountCommand command = new LoginAccountCommand("user@example.com", "incorrect-password");
        AccountPasswordMismatchException failure = new AccountPasswordMismatchException("password does not match");
        when(service.login(command)).thenThrow(failure);

        AccountPasswordMismatchException thrown = assertThrows(
                AccountPasswordMismatchException.class,
                () -> controller.login(command)
        );

        assertSame(failure, thrown);
    }
}
