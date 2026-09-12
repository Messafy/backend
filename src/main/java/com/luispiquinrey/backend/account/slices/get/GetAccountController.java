package com.luispiquinrey.backend.account.slices.get;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.infrastructure.AccountUserDetails;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
public class GetAccountController {

    private static final Logger log = LoggerFactory.getLogger(GetAccountController.class);

    private final GetAccountService service;

    public GetAccountController(GetAccountService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<AccountGetResponse> me(@AuthenticationPrincipal AccountUserDetails principal) {
        log.info("Received request to get current account");
        Account account = service.getMyAccount(principal.account().id());
        log.info("Returning current account {}", account.id().id());
        return ResponseEntity.ok(AccountGetResponse.from(account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountGetResponse> getById(@Valid @RequestBody AccountGetRequest accountGetRequest) {
        log.info("Received request to get account {}", accountGetRequest.id());
        Account account = service.findById(accountGetRequest.id());
        log.info("Returning account {}", account.id().id());
        return ResponseEntity.ok(AccountGetResponse.from(account));
    }
}
