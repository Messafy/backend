package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.application.dto.request.RegisterAccountRequest;
import com.luispiquinrey.backend.account.application.use_cases.RegisterAccountUseCase;
import com.luispiquinrey.backend.account.presentation.presenter.RegisterAccountPresenter;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import com.luispiquinrey.backend.account.presentation.advice.AccountExceptionHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegisterAccountControllerTest {

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldRegisterAtRegistrationPath() throws Exception {
        RegisterAccountUseCase service = mock(RegisterAccountUseCase.class);
        RegisterAccountRequest request = new RegisterAccountRequest("user@example.com", "Password1!", "Locker1!");
        when(service.register(request)).thenReturn(Result.success(accountView()));

        mockMvc(service).perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"Password1!\",\"rawLockerPassword\":\"Locker1!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.publicCode").value("V1StGXR8_Z5j"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.rawPassword").doesNotExist())
                .andExpect(jsonPath("$.rawLockerPassword").doesNotExist());

        verify(service).register(request);
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldReturnConflictWhenAccountAlreadyExists() throws Exception {
        RegisterAccountUseCase service = mock(RegisterAccountUseCase.class);
        when(service.register(new RegisterAccountRequest("user@example.com", "Password1!", "Locker1!")))
                .thenReturn(Result.failure(ApplicationError.conflictError("email already registered")));

        mockMvc(service).perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"Password1!\",\"rawLockerPassword\":\"Locker1!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("email already registered"))
                .andExpect(jsonPath("$.code").value("CONFLICT_ERROR"));
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldRejectInvalidRegistrationPayload() throws Exception {
        RegisterAccountUseCase service = mock(RegisterAccountUseCase.class);

        mockMvc(service).perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"short\",\"rawLockerPassword\":\"Locker1!\"}"))
                .andExpect(status().isBadRequest());
    }

    private MockMvc mockMvc(RegisterAccountUseCase service) {
        return MockMvcBuilders.standaloneSetup(new RegisterAccountController(
                        service, new RegisterAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter()))
                .setControllerAdvice(new AccountExceptionHandler())
                .build();
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldRejectMalformedEmailBeforeInvokingUseCase() throws Exception {
        RegisterAccountUseCase service = mock(RegisterAccountUseCase.class);

        mockMvc(service).perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\",\"rawPassword\":\"Password1!\",\"rawLockerPassword\":\"Locker1!\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    private AccountResponse accountView() {
        return new AccountResponse(
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

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldPresentApplicationRequestValidationWithoutInvokingUseCase() {
        RegisterAccountUseCase service = mock(RegisterAccountUseCase.class);
        var request = Map.of("email", "invalid", "rawPassword", "Password1!", "rawLockerPassword", "Locker1!");

        var response = new RegisterAccountController(
                service, new RegisterAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter()).register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("email must be a valid email address", assertInstanceOf(ErrorViewModel.class, response.getBody()).message());
        verifyNoInteractions(service);
    }

    @Test
    @Timeout(1)
    @Tag("registerAccountController")
    void shouldRejectMissingLockerPasswordWithoutPresentationDtoValidation() throws Exception {
        RegisterAccountUseCase service = mock(RegisterAccountUseCase.class);

        mockMvc(service).perform(post("/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"Password1!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("raw locker password cannot be null"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(service);
    }
}
