package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.api.VerificationTokenIssuer;
import com.luispiquinrey.backend.account.application.port.VerificationTokenCodec;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.exception.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.springframework.stereotype.Service;

@Service
public class VerifyAccountService implements VerificationTokenIssuer {

    private final VerificationTokenCodec verificationTokenCodec;
    private final AccountRepository accountRepository;

    public VerifyAccountService(VerificationTokenCodec verificationTokenCodec, AccountRepository accountRepository) {
        this.verificationTokenCodec = verificationTokenCodec;
        this.accountRepository = accountRepository;
    }

    @Override
    public String issue(String accountId) {
        return verificationTokenCodec.issue(accountId);
    }

    public void verifyByToken(String rawToken) {
        String accountId = verificationTokenCodec.extractAccountId(rawToken);
        Account account = accountRepository.findById(new UserId(accountId))
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.verify();
        accountRepository.save(account);
    }
}
