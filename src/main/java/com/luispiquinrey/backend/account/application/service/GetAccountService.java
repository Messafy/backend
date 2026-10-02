package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.domain.exception.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetAccountService implements AccountLookup {

    private final AccountRepository accountRepository;
    private final PasswordHasher passwordHasher;

    public GetAccountService(AccountRepository accountRepository, PasswordHasher passwordHasher) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
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

    private AccountView findOrFail(UserId id) {
        return accountRepository.findById(id)
                .map(AccountView::from)
                .orElseThrow(() -> new AccountNotFoundException(id.id()));
    }
}