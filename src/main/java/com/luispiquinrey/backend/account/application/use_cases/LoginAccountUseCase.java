package com.luispiquinrey.backend.account.application.use_cases;

import com.luispiquinrey.backend.account.application.dto.request.LoginAccountRequest;
import com.luispiquinrey.backend.account.application.dto.response.LoginAccountResponse;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.application.port.TokenIssuer;
import com.luispiquinrey.backend.account.domain.exception.AccountConflictException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;

import java.util.Optional;

@LogExecution
public class LoginAccountUseCase {

    private final Logger log;

    static final String INVALID_CREDENTIALS = "invalid credentials";

    private final TimingSafePasswordCheck passwordCheck;
    private final TokenIssuer tokenIssuer;
    private final AccountRepository accountRepository;

    public LoginAccountUseCase(PasswordHasher passwordHasher, TokenIssuer tokenIssuer, AccountRepository accountRepository, Logger log) {
        this.passwordCheck = new TimingSafePasswordCheck(passwordHasher);
        this.tokenIssuer = tokenIssuer;
        this.accountRepository = accountRepository;
        this.log = log;
    }

    public Result<LoginAccountResponse> login(LoginAccountRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("login request cannot be null"));
        }

        Email email = request.toEmail();
        log.debug("Authenticating account for {}", email.email());
        Optional<Account> found = accountRepository.findByEmail(email.email());
        EncodedPassword storedHash = found.map(Account::encodedPassword).orElse(null);
        if (!passwordCheck.matches(request.rawPassword(), storedHash)) {
            return invalidCredentials();
        }
        Account account = found.get();

        try {
            account.login();
        } catch (AccountConflictException e) {
            log.debug("Rejected login for account {} because its state does not allow it", account.id().id());
            return invalidCredentials();
        }
        accountRepository.recordLogin(account);

        log.debug("Authenticated account {}", account.id().id());

        return Result.success(new LoginAccountResponse(tokenIssuer.issue(account)));
    }

    private static Result<LoginAccountResponse> invalidCredentials() {
        return Result.failure(ApplicationError.unauthorizedError(INVALID_CREDENTIALS));
    }
}
