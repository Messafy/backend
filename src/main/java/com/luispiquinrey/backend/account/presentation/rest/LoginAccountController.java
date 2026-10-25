package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.use_cases.LoginAccountUseCase;
import com.luispiquinrey.backend.account.application.dto.request.LoginAccountRequest;
import com.luispiquinrey.backend.account.presentation.viewmodel.LoginViewModel;
import com.luispiquinrey.backend.account.presentation.presenter.LoginAccountPresenter;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Public authentication operations: registration, login and email verification.")
public class LoginAccountController {

    private static final Logger log = LoggerFactory.getLogger(LoginAccountController.class);

    private final LoginAccountUseCase loginAccountUseCase;
    private final LoginAccountPresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public LoginAccountController(
            LoginAccountUseCase loginAccountUseCase,
            LoginAccountPresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.loginAccountUseCase = loginAccountUseCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
    }

    @Operation(
            summary = "Log in with email and password",
            description = "Authenticates the credentials and returns a JWT bearer token valid for 24 hours. "
                    + "Use it as 'Authorization: Bearer <token>' on protected endpoints.",
            requestBody = @RequestBody(
                    description = "Login credentials: email address and raw password.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginAccountRequest.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authenticated, JWT returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid login data (blank fields, malformed email)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorViewModel.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials (unknown email or password mismatch)",
                    content = @Content(mediaType = "application/json"))})
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body) {
        log.info("Received login attempt");
        LoginAccountRequest input;
        try {
            input = new LoginAccountRequest(body.get("email"), body.get("rawPassword"));
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        return httpResponsePresenter.toHttp(presenter.present(loginAccountUseCase.login(input)));
    }
}
