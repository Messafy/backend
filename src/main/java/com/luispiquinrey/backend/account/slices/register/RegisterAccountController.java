package com.luispiquinrey.backend.account.slices.register;

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
    public ResponseEntity<AccountRegisterResponse> register(
            @Valid @RequestBody AccountRegisterRequest request) {
        log.info("Received registration request for {}", request.email());
        AccountRegisterResponse response = registerAccountService.register(request);
        log.info("Created account {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
