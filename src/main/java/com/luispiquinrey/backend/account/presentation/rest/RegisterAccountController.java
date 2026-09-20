package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.dto.RegisterAccountCommand;
import com.luispiquinrey.backend.account.application.service.RegisterAccountService;
import com.luispiquinrey.backend.account.presentation.dto.RegisterAccountRequest;
import com.luispiquinrey.backend.account.presentation.dto.RegisterAccountResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class RegisterAccountController {

    private static final Logger log = LoggerFactory.getLogger(RegisterAccountController.class);

    private final RegisterAccountService registerAccountService;

    public RegisterAccountController(RegisterAccountService registerAccountService) {
        this.registerAccountService = registerAccountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RegisterAccountResponse> register(@Valid @RequestBody RegisterAccountRequest request) {
        return register(request.toCommand());
    }

    public ResponseEntity<RegisterAccountResponse> register(RegisterAccountCommand command) {
        log.info("Received registration request for {}", command.email());
        AccountView accountView = registerAccountService.register(command);
        log.info("Created account {}", accountView.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterAccountResponse.from(accountView));
    }
}
