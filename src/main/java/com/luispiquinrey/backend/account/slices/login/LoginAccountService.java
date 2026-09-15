package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.AccountPasswordMismatchException;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.infrastructure.AccountUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginAccountService {

    private static final Logger log = LoggerFactory.getLogger(LoginAccountService.class);

    private final LoginPasswordHasher passwordHasher;
    private final LoginTokenIssuer tokenIssuer;
    private final LoginAccountRepository loginAccountRepository;

    public LoginAccountService(LoginPasswordHasher passwordHasher, LoginTokenIssuer tokenIssuer, LoginAccountRepository loginAccountRepository) {
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.loginAccountRepository = loginAccountRepository;
    }

    public AccountLoginResponse login(AccountLoginRequest request) {
        if (request.rawPassword() == null || request.rawPassword().isBlank()) {
            log.warn("Login rejected for {} because password is missing", request.email());
            throw new IllegalArgumentException("raw password cannot be blank");
        }

        Email email = new Email(request.email());
        log.info("Logging in account {}", email.email());
        Account account = getAccountByEmail(email.email());

        if (!passwordHasher.matches(request.rawPassword(), account.encodedPassword())) {
            log.warn("Login rejected for {} because credentials were invalid", email.email());
            throw new AccountPasswordMismatchException("password does not match");
        }

        account.login();
        loginAccountRepository.save(account);
        log.info("Logged in account {}", account.id().id());

        return new AccountLoginResponse(tokenIssuer.issue(new AccountUserDetails(account)));
    }

    private Account getAccountByEmail(String email) {
        return loginAccountRepository.byEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login rejected because account {} was not found", email);
                    return new UsernameNotFoundException(email);
                });
    }
}
