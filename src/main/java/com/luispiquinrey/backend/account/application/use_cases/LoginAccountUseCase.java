package com.luispiquinrey.backend.account.application.use_cases;

import com.luispiquinrey.backend.account.application.dto.request.LoginAccountRequest;
import com.luispiquinrey.backend.account.application.dto.response.LoginAccountResponse;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.application.port.TokenIssuer;
import com.luispiquinrey.backend.account.domain.exception.AccountConflictException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;

@LogExecution
public class LoginAccountUseCase {

    private final Logger log;

    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final AccountRepository accountRepository;

    public LoginAccountUseCase(PasswordHasher passwordHasher, TokenIssuer tokenIssuer, AccountRepository accountRepository, Logger log) {
        this.passwordHasher = passwordHasher;
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
        var found = accountRepository.findByEmail(email.email());
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.unauthorizedError("invalid credentials"));
        }
        Account account = found.get();

        if (!passwordHasher.matches(request.rawPassword(), account.encodedPassword())) {
            return Result.failure(ApplicationError.unauthorizedError("invalid credentials"));
        }

        try {
            account.login();
            accountRepository.save(account);
        } catch (AccountConflictException e) {
            return Result.failure(ApplicationError.conflictError(e.getMessage()));
        }

        log.debug("Authenticated account {}", account.id().id());

        return Result.success(new LoginAccountResponse(tokenIssuer.issue(account)));
    }
}
