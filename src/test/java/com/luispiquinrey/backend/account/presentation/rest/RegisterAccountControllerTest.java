package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.dto.RegisterAccountCommand;
import com.luispiquinrey.backend.account.application.service.RegisterAccountService;
import com.luispiquinrey.backend.account.domain.exception.AccountConflictException;
import com.luispiquinrey.backend.account.presentation.advice.AccountExceptionHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegisterAccountControllerTest {

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldRegisterAtRegistrationPath() throws Exception {
        RegisterAccountService service = mock(RegisterAccountService.class);
        RegisterAccountCommand command = new RegisterAccountCommand("user@example.com", "Password1!", "Locker1!");
        when(service.register(command)).thenReturn(accountView());

        mockMvc(service).perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"Password1!\",\"rawLockerPassword\":\"Locker1!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.publicCode").value("V1StGXR8_Z5j"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(service).register(command);
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldReturnConflictWhenAccountAlreadyExists() throws Exception {
        RegisterAccountService service = mock(RegisterAccountService.class);
        when(service.register(new RegisterAccountCommand("user@example.com", "Password1!", "Locker1!")))
                .thenThrow(new AccountConflictException("email already registered"));

        mockMvc(service).perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"Password1!\",\"rawLockerPassword\":\"Locker1!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("email already registered"));
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldRejectInvalidRegistrationPayload() throws Exception {
        RegisterAccountService service = mock(RegisterAccountService.class);

        mockMvc(service).perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"short\",\"rawLockerPassword\":\"Locker1!\"}"))
                .andExpect(status().isBadRequest());
    }

    private MockMvc mockMvc(RegisterAccountService service) {
        return MockMvcBuilders.standaloneSetup(new RegisterAccountController(service))
                .setControllerAdvice(new AccountExceptionHandler())
                .build();
    }

    private AccountView accountView() {
        return new AccountView(
                "507f1f77bcf86cd799439011",
                "user@example.com",
                "V1StGXR8_Z5j",
                "USER",
                "ACTIVE",
                false,
                Instant.parse("2026-09-12T10:15:30Z"),
                null
        );
    }
}
