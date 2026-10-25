package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.request.LoginAccountRequest;
import com.luispiquinrey.backend.account.application.dto.response.LoginAccountResponse;
import com.luispiquinrey.backend.account.application.use_cases.LoginAccountUseCase;
import com.luispiquinrey.backend.account.presentation.presenter.LoginAccountPresenter;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.account.presentation.viewmodel.LoginViewModel;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import com.luispiquinrey.backend.account.presentation.advice.AccountExceptionHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginAccountControllerTest {

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldNotLogUntrustedEmail(CapturedOutput output) {
        LoginAccountUseCase service = mock(LoginAccountUseCase.class);
        var request = Map.of("email", "user@example.com\r\nFORGED_LOGIN", "rawPassword", "password");

        var response = new LoginAccountController(
                service, new LoginAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter()).login(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verifyNoInteractions(service);
        assertFalse(output.getAll().contains("FORGED_LOGIN"));
        assertFalse(output.getAll().contains("user@example.com"));
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldLoginAtLoginPath() throws Exception {
        LoginAccountUseCase service = mock(LoginAccountUseCase.class);
        when(service.login(new LoginAccountRequest("user@example.com", "correct-password")))
                .thenReturn(Result.success(new LoginAccountResponse("opaque-login-token")));

        mockMvc(service).perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("opaque-login-token"))
                .andExpect(jsonPath("$.value").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(service).login(new LoginAccountRequest("user@example.com", "correct-password"));
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        LoginAccountUseCase service = mock(LoginAccountUseCase.class);
        when(service.login(new LoginAccountRequest("user@example.com", "incorrect-password")))
                .thenReturn(Result.failure(ApplicationError.unauthorizedError("invalid credentials")));

        mockMvc(service).perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"incorrect-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid credentials"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_ERROR"));
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldRejectBlankPassword() throws Exception {
        LoginAccountUseCase service = mock(LoginAccountUseCase.class);

        mockMvc(service).perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    private MockMvc mockMvc(LoginAccountUseCase service) {
        return MockMvcBuilders.standaloneSetup(new LoginAccountController(
                        service, new LoginAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter()))
                .setControllerAdvice(new AccountExceptionHandler())
                .build();
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldNotMisclassifyUnexpectedUseCaseExceptionsAsInputValidation() {
        LoginAccountUseCase service = mock(LoginAccountUseCase.class);
        var request = Map.of("email", "user@example.com", "rawPassword", "password");
        when(service.login(new LoginAccountRequest("user@example.com", "password")))
                .thenThrow(new IllegalArgumentException("invalid stored data"));

        assertThrows(IllegalArgumentException.class,
                () -> new LoginAccountController(
                        service, new LoginAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter()).login(request));
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldUseItsOwnInjectedPresenterAndExposeOnlyItsView() {
        LoginAccountUseCase service = mock(LoginAccountUseCase.class);
        LoginAccountPresenter presenter = mock(LoginAccountPresenter.class);
        var applicationResponse = new LoginAccountResponse("opaque-token");
        var result = Result.success(applicationResponse);
        var view = new LoginViewModel("formatted-token");
        var input = new LoginAccountRequest("user@example.com", "password");
        when(service.login(input)).thenReturn(result);
        when(presenter.present(result)).thenReturn(OperationResult.succeed(view));

        var response = new LoginAccountController(service, presenter, new HttpResponsePresenter())
                .login(Map.of("email", "user@example.com", "rawPassword", "password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(view, response.getBody());
        verify(presenter).present(result);
    }

    @Test
    @Timeout(1)
    @Tag("loginAccountController")
    void shouldRejectMissingAndNullFieldsBeforeExecutingUseCase() throws Exception {
        LoginAccountUseCase service = mock(LoginAccountUseCase.class);
        MockMvc mvc = mockMvc(service);
        for (String payload : new String[]{"{}", "{\"email\":\"user@example.com\"}",
                "{\"email\":null,\"rawPassword\":\"password\"}",
                "{\"email\":\"user@example.com\",\"rawPassword\":null}"}) {
            mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        verifyNoInteractions(service);
    }
}
