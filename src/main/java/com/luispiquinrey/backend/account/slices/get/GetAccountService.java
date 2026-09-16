package com.luispiquinrey.backend.account.slices.get;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.AccountNotFoundException;
import com.luispiquinrey.backend.share.identity.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetAccountService implements AccountLookup {

    private static final Logger log = LoggerFactory.getLogger(GetAccountService.class);

    private final GetAccountRepository repository;

    public GetAccountService(GetAccountRepository repository) {
        this.repository = repository;
    }

    public Account getMyAccount(UserId id) {
        log.debug("Looking up current account {}", id.id());
        return findOrFail(id);
    }

    public Account findById(String id) {
        log.debug("Looking up account {}", id);
        return findOrFail(new UserId(id));
    }

    public Account findByEmail(String email) {
        log.debug("Looking up account with email {}", email);
        return repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Account with email {} was not found", email);
                    return new AccountNotFoundException(email);
                });
    }

    @Override
    public boolean existsActiveAccount(String accountId) {
        return repository.findById(new UserId(accountId))
                .filter(Account::isActive)
                .isPresent();
    }

    private Account findOrFail(UserId id) {
        Optional<Account> account = repository.findById(id);
        return account.orElseThrow(() -> {
            log.warn("Account {} was not found", id.id());
            return new AccountNotFoundException(id.id());
        });
    }
}
