package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.request.ResendVerificationRequest;
import com.luispiquinrey.backend.account.application.use_cases.ResendVerificationUseCase;
import com.luispiquinrey.backend.account.presentation.presenter.ResendVerificationPresenter;
import com.luispiquinrey.backend.account.presentation.request.ResendVerificationBody;
import com.luispiquinrey.backend.account.presentation.viewmodel.ResendVerificationViewModel;
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

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Public authentication operations: registration, login and email verification.")
public class ResendVerificationController {

    private static final Logger log = LoggerFactory.getLogger(ResendVerificationController.class);

    private final ResendVerificationUseCase resendVerificationUseCase;
    private final ResendVerificationPresenter presenter;
    private final HttpResponsePresenter httpResponsePresenter;

    public ResendVerificationController(
            ResendVerificationUseCase resendVerificationUseCase,
            ResendVerificationPresenter presenter,
            HttpResponsePresenter httpResponsePresenter) {
        this.resendVerificationUseCase = resendVerificationUseCase;
        this.presenter = presenter;
        this.httpResponsePresenter = httpResponsePresenter;
    }

    @Operation(
            summary = "Send a new verification email",
            description = "Sends a fresh verification link when an active, unverified account uses this email. "
                    + "The response is the same whether or not such an account exists.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResendVerificationBody.class))))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Request accepted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResendVerificationViewModel.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed email",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ErrorViewModel.class)))})
    @PostMapping("/verification/resend")
    public ResponseEntity<?> resend(
            @org.springframework.web.bind.annotation.RequestBody ResendVerificationBody body) {
        log.info("Received verification email resend request");
        ResendVerificationRequest input;
        try {
            input = new ResendVerificationRequest(body.email());
        } catch (IllegalArgumentException e) {
            return httpResponsePresenter.toHttp(
                    presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        return httpResponsePresenter.toHttp(
                presenter.present(resendVerificationUseCase.resend(input)),
                view -> ResponseEntity.status(HttpStatus.ACCEPTED).body(view));
    }
}
