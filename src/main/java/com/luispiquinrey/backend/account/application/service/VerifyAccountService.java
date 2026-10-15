package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.api.VerificationTokenIssuer;
import com.luispiquinrey.backend.account.application.port.VerificationTokenCodec;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.exception.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;

@LogExecution
public class VerifyAccountService implements VerificationTokenIssuer {

    private final Logger log;

    private final VerificationTokenCodec verificationTokenCodec;
    private final AccountRepository accountRepository;

    public VerifyAccountService(VerificationTokenCodec verificationTokenCodec, AccountRepository accountRepository, Logger log) {
        this.verificationTokenCodec = verificationTokenCodec;
        this.accountRepository = accountRepository;
        this.log = log;
    }

    @Override
    public String issue(String accountId) {
        log.debug("Issuing verification token for account {}", accountId);
        return verificationTokenCodec.issue(accountId);
    }

    public void verifyByToken(String rawToken) {
        log.debug("Verifying account by token");
        String accountId = verificationTokenCodec.extractAccountId(rawToken);
        Account account = accountRepository.findById(new UserId(accountId))
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.verify();
        accountRepository.save(account);
        log.debug("Verified account {}", accountId);
    }
}
