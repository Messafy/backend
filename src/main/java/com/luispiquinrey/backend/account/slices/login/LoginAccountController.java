package com.luispiquinrey.backend.account.slices.login;

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
    public ResponseEntity<AccountLoginResponse> login(
            @Valid @RequestBody AccountLoginRequest request) {
        log.info("Received login attempt for {}", request.email());
        AccountLoginResponse response =
                loginAccountService.login(request);
        log.info("Login successful for {}", request.email());
        return ResponseEntity.ok(response);
    }
}
