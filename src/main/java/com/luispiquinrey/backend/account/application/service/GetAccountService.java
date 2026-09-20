package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.domain.exception.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.springframework.stereotype.Service;

@Service
public class GetAccountService implements AccountLookup {

    private final AccountRepository accountRepository;

    public GetAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountView getMyAccount(UserId id) {
        return findOrFail(id);
    }

    public AccountView findById(String id) {
        return findOrFail(new UserId(id));
    }

    @Override
    public boolean existsActiveAccount(String accountId) {
        return accountRepository.findById(new UserId(accountId))
                .filter(Account::isActive)
                .isPresent();
    }

    private AccountView findOrFail(UserId id) {
        return accountRepository.findById(id)
                .map(AccountView::from)
                .orElseThrow(() -> new AccountNotFoundException(id.id()));
    }
}
