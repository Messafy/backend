package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.domain.AccountPasswordMismatchException;
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
        AccountLoginRequest request = new AccountLoginRequest("user@example.com", "correct-password");
        AccountLoginResponse loginResponse = new AccountLoginResponse("opaque-login-token");
        when(service.login(request)).thenReturn(loginResponse);

        ResponseEntity<AccountLoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(loginResponse, response.getBody());
        verify(service).login(request);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldPropagateLoginFailure() {
        LoginAccountService service = mock(LoginAccountService.class);
        LoginAccountController controller = new LoginAccountController(service);
        AccountLoginRequest request = new AccountLoginRequest("user@example.com", "incorrect-password");
        AccountPasswordMismatchException failure = new AccountPasswordMismatchException("password does not match");
        when(service.login(request)).thenThrow(failure);

        AccountPasswordMismatchException thrown = assertThrows(
                AccountPasswordMismatchException.class,
                () -> controller.login(request)
        );

        assertSame(failure, thrown);
    }
}
