package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.service.VerifyAccountService;
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

    private final VerifyAccountService verifyAccountService;
    private final String frontendUrl;

    public VerifyAccountController(
            VerifyAccountService verifyAccountService,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.verifyAccountService = verifyAccountService;
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
        try {
            verifyAccountService.verifyByToken(token);
            log.info("Account verification succeeded");
            return new RedirectView(frontendUrl + "/login?verification=success", false);
        } catch (RuntimeException e) {
            log.warn("Account verification failed");
            return new RedirectView(frontendUrl + "/login?verification=failed", false);
        }
    }
}
