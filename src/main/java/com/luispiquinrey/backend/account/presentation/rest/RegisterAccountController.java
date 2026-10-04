package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.dto.RegisterAccountCommand;
import com.luispiquinrey.backend.account.application.service.RegisterAccountService;
import com.luispiquinrey.backend.account.presentation.dto.RegisterAccountRequest;
import com.luispiquinrey.backend.account.presentation.dto.RegisterAccountResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Public authentication operations: registration, login and email verification.")
public class RegisterAccountController {

    private static final Logger log = LoggerFactory.getLogger(RegisterAccountController.class);

    private final RegisterAccountService registerAccountService;

    public RegisterAccountController(RegisterAccountService registerAccountService) {
        this.registerAccountService = registerAccountService;
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
                            schema = @Schema(implementation = RegisterAccountResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data (blank fields, malformed email, weak locker password)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "An account with the same email or public code already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))})
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RegisterAccountResponse> register(
            @Valid @org.springframework.web.bind.annotation.RequestBody RegisterAccountRequest request) {
        return register(request.toCommand());
    }

    public ResponseEntity<RegisterAccountResponse> register(RegisterAccountCommand command) {
        log.info("Received registration request for {}", command.email());
        AccountView accountView = registerAccountService.register(command);
        log.info("Created account {}", accountView.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterAccountResponse.from(accountView));
    }
}
