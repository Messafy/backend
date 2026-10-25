package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.use_cases.VerifyAccountUseCase;
import com.luispiquinrey.backend.account.application.dto.request.VerifyAccountRequest;
import com.luispiquinrey.backend.account.presentation.presenter.VerifyAccountPresenter;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;
import com.luispiquinrey.backend.share.presentation.OperationResult;
import com.luispiquinrey.backend.account.presentation.viewmodel.VerificationViewModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Public authentication operations: registration, login and email verification.")
public class VerifyAccountController {

    private static final Logger log = LoggerFactory.getLogger(VerifyAccountController.class);

    private final VerifyAccountUseCase verifyAccountUseCase;
    private final VerifyAccountPresenter presenter;
    private final String frontendUrl;

    public VerifyAccountController(
            VerifyAccountUseCase verifyAccountUseCase,
            VerifyAccountPresenter presenter,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.verifyAccountUseCase = verifyAccountUseCase;
        this.presenter = presenter;
        this.frontendUrl = frontendUrl;
    }

    @Operation(
            summary = "Verify an account from the email link",
            description = "Validates the one-time verification token sent by email, marks the account as "
                    + "verified and redirects the browser to the frontend login page with "
                    + "'?verification=success' or '?verification=failed'. The token itself is never logged.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirect to the frontend login page with the verification outcome")})
    @GetMapping("/verify")
    public RedirectView verify(
            @Parameter(description = "One-time verification JWT received by email")
            @RequestParam("token") String token) {
        log.info("Received account verification request");
        VerifyAccountRequest input;
        try {
            input = new VerifyAccountRequest(token);
        } catch (IllegalArgumentException e) {
            return redirect(presenter.present(Result.failure(ApplicationError.validationError(e.getMessage()))));
        }
        try {
            return redirect(presenter.present(verifyAccountUseCase.verifyByToken(input)));
        } catch (RuntimeException e) {
            log.warn("Account verification failed");
            return new RedirectView(frontendUrl + "/login?verification=failed", false);
        }
    }

    private RedirectView redirect(OperationResult<VerificationViewModel> outcome) {
        if (outcome.isFailure() || !outcome.getValue().verified()) {
            log.warn("Account verification failed");
            return new RedirectView(frontendUrl + "/login?verification=failed", false);
        }
        log.info("Account verification succeeded");
        return new RedirectView(frontendUrl + "/login?verification=success", false);
    }
}
