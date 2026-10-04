package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.LoginAccountCommand;
import com.luispiquinrey.backend.account.application.service.LoginAccountService;
import com.luispiquinrey.backend.account.domain.exception.AccountPasswordMismatchException;
import com.luispiquinrey.backend.account.presentation.advice.AccountExceptionHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginAccountControllerTest {

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldLoginAtLoginPath() throws Exception {
        LoginAccountService service = mock(LoginAccountService.class);
        when(service.login(new LoginAccountCommand("user@example.com", "correct-password")))
                .thenReturn("opaque-login-token");

        mockMvc(service).perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("opaque-login-token"));

        verify(service).login(new LoginAccountCommand("user@example.com", "correct-password"));
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        LoginAccountService service = mock(LoginAccountService.class);
        when(service.login(new LoginAccountCommand("user@example.com", "incorrect-password")))
                .thenThrow(new AccountPasswordMismatchException("password does not match"));

        mockMvc(service).perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"incorrect-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("invalid credentials"));
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldRejectBlankPassword() throws Exception {
        LoginAccountService service = mock(LoginAccountService.class);

        mockMvc(service).perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    private MockMvc mockMvc(LoginAccountService service) {
        return MockMvcBuilders.standaloneSetup(new LoginAccountController(service))
                .setControllerAdvice(new AccountExceptionHandler())
                .build();
    }
}
