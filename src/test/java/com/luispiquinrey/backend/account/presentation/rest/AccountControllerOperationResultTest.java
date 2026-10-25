package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.request.GetAccountRequest;
import com.luispiquinrey.backend.account.application.dto.request.LoginAccountRequest;
import com.luispiquinrey.backend.account.application.dto.request.RegisterAccountRequest;
import com.luispiquinrey.backend.account.application.use_cases.GetAccountUseCase;
import com.luispiquinrey.backend.account.application.use_cases.LoginAccountUseCase;
import com.luispiquinrey.backend.account.application.use_cases.RegisterAccountUseCase;
import com.luispiquinrey.backend.account.presentation.presenter.GetAccountPresenter;
import com.luispiquinrey.backend.account.presentation.presenter.LoginAccountPresenter;
import com.luispiquinrey.backend.account.presentation.presenter.RegisterAccountPresenter;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.ErrorType;
import com.luispiquinrey.backend.share.result.Result;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountControllerOperationResultTest {
    private static final Map<ErrorType, Integer> STATUSES = Map.of(
            ErrorType.VALIDATION_ERROR, 400,
            ErrorType.NOT_FOUND_ERROR, 404,
            ErrorType.UNAUTHORIZED_ERROR, 401,
            ErrorType.FORBIDDEN_ERROR, 403,
            ErrorType.CONFLICT_ERROR, 409,
            ErrorType.INTERNAL_ERROR, 500);

    @Test
    @Tag("accountControllerOperationResult")
    @Timeout(1)
    void getControllerTranslatesPresentedErrorsToHttp() {
        var service = mock(GetAccountUseCase.class);
        var controller = new GetAccountController(
                service, new GetAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter());

        for (ErrorType type : ErrorType.values()) {
            when(service.findById(new GetAccountRequest("opaque-id")))
                    .thenReturn(Result.failure(new ApplicationError(type, "Operation failed")));

            assertPublicError(controller.getById("opaque-id"), type);
        }
    }

    @Test
    @Tag("accountControllerOperationResult")
    @Timeout(1)
    void loginControllerTranslatesPresentedErrorsToHttp() {
        var service = mock(LoginAccountUseCase.class);
        var controller = new LoginAccountController(
                service, new LoginAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter());

        for (ErrorType type : ErrorType.values()) {
            when(service.login(new LoginAccountRequest("user@example.com", "password")))
                    .thenReturn(Result.failure(new ApplicationError(type, "Operation failed")));

            assertPublicError(controller.login(Map.of("email", "user@example.com", "rawPassword", "password")), type);
        }
    }

    @Test
    @Tag("accountControllerOperationResult")
    @Timeout(1)
    void registerControllerTranslatesPresentedErrorsToHttp() {
        var service = mock(RegisterAccountUseCase.class);
        var controller = new RegisterAccountController(
                service, new RegisterAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter());

        for (ErrorType type : ErrorType.values()) {
            when(service.register(new RegisterAccountRequest("user@example.com", "Password1!", "Locker1!")))
                    .thenReturn(Result.failure(new ApplicationError(type, "Operation failed")));

            assertPublicError(controller.register(Map.of("email", "user@example.com",
                    "rawPassword", "Password1!", "rawLockerPassword", "Locker1!")), type);
        }
    }

    private void assertPublicError(ResponseEntity<?> response, ErrorType type) {
        assertEquals(STATUSES.get(type).intValue(), response.getStatusCode().value());
        var error = assertInstanceOf(ErrorViewModel.class, response.getBody());
        assertEquals(type.name(), error.code());
        assertEquals(type == ErrorType.INTERNAL_ERROR ? "An internal error occurred" : "Operation failed", error.message());
    }
}
