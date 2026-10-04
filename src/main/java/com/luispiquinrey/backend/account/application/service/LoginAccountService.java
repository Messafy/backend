package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.application.dto.LoginAccountCommand;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.application.port.TokenIssuer;
import com.luispiquinrey.backend.account.domain.exception.AccountAuthenticationException;
import com.luispiquinrey.backend.account.domain.exception.AccountPasswordMismatchException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoginAccountService {

    private static final Logger log = LoggerFactory.getLogger(LoginAccountService.class);

    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final AccountRepository accountRepository;

    public LoginAccountService(PasswordHasher passwordHasher, TokenIssuer tokenIssuer, AccountRepository accountRepository) {
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.accountRepository = accountRepository;
    }

    public String login(LoginAccountCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("login command cannot be null");
        }

        if (command.rawPassword() == null || command.rawPassword().isBlank()) {
            throw new IllegalArgumentException("raw password cannot be blank");
        }

        Email email = new Email(command.email());
        log.debug("Authenticating account for {}", email.email());
        Account account = getAccountByEmail(email.email());

        if (!passwordHasher.matches(command.rawPassword(), account.encodedPassword())) {
            throw new AccountPasswordMismatchException("password does not match");
        }

        account.login();
        accountRepository.save(account);

        log.debug("Authenticated account {}", account.id().id());

        return tokenIssuer.issue(account);
    }

    private Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountAuthenticationException("invalid credentials"));
    }
}
