package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.use_cases.RegisterAccountUseCase;
import com.luispiquinrey.backend.account.application.dto.request.RegisterAccountRequest;
import com.luispiquinrey.backend.account.presentation.viewmodel.RegisteredAccountViewModel;
import com.luispiquinrey.backend.account.presentation.presenter.RegisterAccountPresenter;
import com.luispiquinrey.backend.share.presentation.ErrorViewModel;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Public authentication operations: registration, login and email verification.")
public class RegisterAccountController {

    private static final Logger log = LoggerFactory.getLogger(RegisterAccountController.class);

    private final RegisterAccountUseCase registerAccountUseCase;
    private final RegisterAccountPresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public RegisterAccountController(
            RegisterAccountUseCase registerAccountUseCase,
            RegisterAccountPresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.registerAccountUseCase = registerAccountUseCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
    }

    @Operation(
            summary = "Register a new account",
            description = "Creates an account with email, password and locker password, then emits an "
                    + "AccountRegistered event that triggers the verification email.",
            requestBody = @RequestBody(
                    description = "Registration data: email address, login password and locker password "
                            + "used to receive shared notes.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterAccountRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisteredAccountViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data (blank fields, malformed email, weak locker password)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "An account with the same email or public code already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body) {
        log.info("Received registration request");
        RegisterAccountRequest input;
        try {
            input = new RegisterAccountRequest(body.get("email"), body.get("rawPassword"), body.get("rawLockerPassword"));
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))),
                    view -> ResponseEntity.status(HttpStatus.CREATED).body(view));
        }
        return httpResponsePresenter.toHttp(
                presenter.present(registerAccountUseCase.register(input)),
                view -> ResponseEntity.status(HttpStatus.CREATED).body(view));
    }
}
