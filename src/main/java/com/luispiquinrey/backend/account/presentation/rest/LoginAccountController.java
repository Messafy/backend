package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.LoginAccountCommand;
import com.luispiquinrey.backend.account.application.service.LoginAccountService;
import com.luispiquinrey.backend.account.presentation.dto.LoginAccountRequest;
import com.luispiquinrey.backend.account.presentation.dto.LoginAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Public authentication operations: registration, login and email verification.")
public class LoginAccountController {

    private static final Logger log = LoggerFactory.getLogger(LoginAccountController.class);

    private final LoginAccountService loginAccountService;

    public LoginAccountController(LoginAccountService loginAccountService) {
        this.loginAccountService = loginAccountService;
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
                            schema = @Schema(implementation = LoginAccountResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid login data (blank fields, malformed email)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials (unknown email or password mismatch)",
                    content = @Content(mediaType = "application/json"))})
    @PostMapping("/login")
    public ResponseEntity<LoginAccountResponse> login(
            @Valid @org.springframework.web.bind.annotation.RequestBody LoginAccountRequest request) {
        return login(request.toCommand());
    }

    public ResponseEntity<LoginAccountResponse> login(LoginAccountCommand command) {
        log.info("Received login attempt");
        String token = loginAccountService.login(command);
        log.info("Login successful");
        return ResponseEntity.ok(new LoginAccountResponse(token));
    }
}
