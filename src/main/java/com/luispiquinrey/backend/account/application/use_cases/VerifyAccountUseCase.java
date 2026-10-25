package com.luispiquinrey.backend.account.application.use_cases;

import com.luispiquinrey.backend.account.api.VerificationTokenIssuer;
import com.luispiquinrey.backend.account.application.dto.request.VerifyAccountRequest;
import com.luispiquinrey.backend.account.application.dto.response.VerifyAccountResponse;
import com.luispiquinrey.backend.account.application.port.VerificationTokenCodec;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.exception.AccountConflictException;
import com.luispiquinrey.backend.account.domain.exception.AccountVerificationTokenException;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;

@LogExecution
public class VerifyAccountUseCase implements VerificationTokenIssuer {

    private final Logger log;

    private final VerificationTokenCodec verificationTokenCodec;
    private final AccountRepository accountRepository;

    public VerifyAccountUseCase(VerificationTokenCodec verificationTokenCodec, AccountRepository accountRepository, Logger log) {
        this.verificationTokenCodec = verificationTokenCodec;
        this.accountRepository = accountRepository;
        this.log = log;
    }

    @Override
    public String issue(String accountId) {
        log.debug("Issuing verification token for account {}", accountId);
        return verificationTokenCodec.issue(accountId);
    }

    public Result<VerifyAccountResponse> verifyByToken(VerifyAccountRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("verify account request cannot be null"));
        }
        log.debug("Verifying account by token");
        String accountId;
        try {
            accountId = verificationTokenCodec.extractAccountId(request.token());
        } catch (AccountVerificationTokenException e) {
            return Result.failure(ApplicationError.validationError("invalid verification token"));
        }
        var found = accountRepository.findById(new UserId(accountId));
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFoundError("account not found: " + accountId));
        }
        Account account = found.get();
        try {
            account.verify();
            accountRepository.save(account);
        } catch (AccountConflictException e) {
            return Result.failure(ApplicationError.conflictError(e.getMessage()));
        }
        log.debug("Verified account {}", accountId);
        return Result.success(VerifyAccountResponse.from(account));
    }
}
