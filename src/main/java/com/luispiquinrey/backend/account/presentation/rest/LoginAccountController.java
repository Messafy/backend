package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.LoginAccountCommand;
import com.luispiquinrey.backend.account.application.service.LoginAccountService;
import com.luispiquinrey.backend.account.presentation.dto.LoginAccountRequest;
import com.luispiquinrey.backend.account.presentation.dto.LoginAccountResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class LoginAccountController {

    private static final Logger log = LoggerFactory.getLogger(LoginAccountController.class);

    private final LoginAccountService loginAccountService;

    public LoginAccountController(LoginAccountService loginAccountService) {
        this.loginAccountService = loginAccountService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginAccountResponse> login(@Valid @RequestBody LoginAccountRequest request) {
        return login(request.toCommand());
    }

    public ResponseEntity<LoginAccountResponse> login(LoginAccountCommand command) {
        log.info("Received login attempt for {}", command.email());
        String token = loginAccountService.login(command);
        log.info("Login successful for {}", command.email());
        return ResponseEntity.ok(new LoginAccountResponse(token));
    }
}
