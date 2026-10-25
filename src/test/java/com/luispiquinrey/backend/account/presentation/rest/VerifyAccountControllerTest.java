package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.use_cases.VerifyAccountUseCase;
import com.luispiquinrey.backend.account.presentation.presenter.VerifyAccountPresenter;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.account.application.dto.request.VerifyAccountRequest;
import com.luispiquinrey.backend.account.application.dto.response.VerifyAccountResponse;
import com.luispiquinrey.backend.share.result.Result;
import com.luispiquinrey.backend.share.result.ApplicationError;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VerifyAccountControllerTest {

    private static final String FRONTEND_URL = "http://localhost:5173";

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldNotLogUntrustedExceptionMessage(CapturedOutput output) {
        VerifyAccountUseCase service = mock(VerifyAccountUseCase.class);
        doThrow(new IllegalArgumentException("invalid token\r\nFORGED_VERIFICATION"))
                .when(service).verifyByToken(new VerifyAccountRequest("invalid-token"));

        var response = new VerifyAccountController(service, new VerifyAccountPresenter(new ErrorPresenter()), FRONTEND_URL).verify("invalid-token");

        assertEquals(FRONTEND_URL + "/login?verification=failed", response.getUrl());
        assertFalse(output.getAll().contains("FORGED_VERIFICATION"));
        assertFalse(output.getAll().contains("invalid token"));
    }

    @Test
    @Timeout(3)
    @Tag("verifyAccountController")
    void shouldRedirectToSuccessfulVerificationAtVerificationPath() throws Exception {
        VerifyAccountUseCase service = mock(VerifyAccountUseCase.class);
        when(service.verifyByToken(new VerifyAccountRequest("valid-token"))).thenReturn(Result.success(new VerifyAccountResponse(true)));

        mockMvc(service).perform(get("/v1/auth/verify").param("token", "valid-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(FRONTEND_URL + "/login?verification=success"));

        verify(service).verifyByToken(new VerifyAccountRequest("valid-token"));
    }

    @Test
    @Timeout(3)
    @Tag("verifyAccountController")
    void shouldRedirectToFailedVerificationWhenUseCaseReturnsFailure() throws Exception {
        VerifyAccountUseCase service = mock(VerifyAccountUseCase.class);
        when(service.verifyByToken(new VerifyAccountRequest("invalid-token")))
                .thenReturn(Result.failure(ApplicationError.validationError("invalid verification token")));

        mockMvc(service).perform(get("/v1/auth/verify").param("token", "invalid-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(FRONTEND_URL + "/login?verification=failed"));
    }

    @Test
    @Timeout(3)
    @Tag("verifyAccountController")
    void shouldRequireVerificationToken() throws Exception {
        VerifyAccountUseCase service = mock(VerifyAccountUseCase.class);

        mockMvc(service).perform(get("/v1/auth/verify"))
                .andExpect(status().isBadRequest());
    }

    private MockMvc mockMvc(VerifyAccountUseCase service) {
        return MockMvcBuilders.standaloneSetup(new VerifyAccountController(service, new VerifyAccountPresenter(new ErrorPresenter()), FRONTEND_URL)).build();
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountController")
    void shouldPresentBlankTokenValidationWithoutInvokingUseCase() throws Exception {
        VerifyAccountUseCase service = mock(VerifyAccountUseCase.class);

        mockMvc(service).perform(get("/v1/auth/verify").param("token", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(FRONTEND_URL + "/login?verification=failed"));

        verifyNoInteractions(service);
    }
}
