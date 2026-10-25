package com.luispiquinrey.backend.account.application.use_cases;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.application.dto.request.GetAccountRequest;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;

import java.util.Optional;

@LogExecution
public class GetAccountUseCase implements AccountLookup {

    private final Logger log;
    private final AccountRepository accountRepository;
    private final PasswordHasher passwordHasher;

    public GetAccountUseCase(AccountRepository accountRepository, PasswordHasher passwordHasher, Logger log) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
        this.log = log;
    }

    public Result<AccountResponse> getMyAccount(GetAccountRequest request) {
        return findById(request);
    }

    public Result<AccountResponse> findById(GetAccountRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("get account request cannot be null"));
        }
        UserId id = request.toUserId();
        log.debug("Loading account {}", id.id());
        return accountRepository.findById(id)
                .map(account -> Result.success(AccountResponse.from(account)))
                .orElseGet(() -> Result.failure(ApplicationError.notFoundError("account not found: " + id.id())));
    }

    @Override
    public boolean existsActiveAccount(String accountId) {
        return accountRepository.findById(new UserId(accountId))
                .filter(Account::isActive)
                .isPresent();
    }

    @Override
    public Optional<UserId> findActiveAccountIdByPublicCode(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            return Optional.empty();
        }
        return accountRepository.findByPublicCode(publicCode)
                .filter(Account::isActive)
                .map(Account::id);
    }

    @Override
    public boolean matchesLockerPassword(UserId accountId, String rawLockerPassword) {
        if (accountId == null || rawLockerPassword == null || rawLockerPassword.isBlank()) {
            return false;
        }
        return accountRepository.findById(accountId)
                .map(account -> passwordHasher.matches(
                        rawLockerPassword,
                        new EncodedPassword(account.lockerPassword().value())
                ))
                .orElse(false);
    }

}
