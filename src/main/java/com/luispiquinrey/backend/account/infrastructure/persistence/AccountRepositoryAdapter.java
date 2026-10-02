package com.luispiquinrey.backend.account.infrastructure.persistence;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private static final Logger log = LoggerFactory.getLogger(AccountRepositoryAdapter.class);

    private final MongoDataAccountRepository mongoDataAccountRepository;
    private final AccountMapper accountMapper;

    public AccountRepositoryAdapter(MongoDataAccountRepository mongoDataAccountRepository, AccountMapper accountMapper) {
        this.mongoDataAccountRepository = mongoDataAccountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public Optional<Account> findById(UserId id) {
        log.debug("Querying MongoDB for account {}", id.id());
        Optional<Account> account = mongoDataAccountRepository.findById(accountMapper.toObjectId(id.id()))
                .map(accountMapper::toDomain);
        log.debug("MongoDB {} account {}", account.isPresent() ? "found" : "did not find", id.id());
        return account;
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        log.debug("Querying MongoDB for account with email {}", email);
        Optional<Account> account = mongoDataAccountRepository.findByEmail(email)
                .map(accountMapper::toDomain);
        log.debug("MongoDB {} account with email {}", account.isPresent() ? "found" : "did not find", email);
        return account;
    }

    @Override
    public Optional<Account> findByPublicCode(String publicCode) {
        log.debug("Querying MongoDB for account with public code {}", publicCode);
        Optional<Account> account = mongoDataAccountRepository.findByPublicCode(publicCode)
                .map(accountMapper::toDomain);
        log.debug("MongoDB {} account with public code {}", account.isPresent() ? "found" : "did not find", publicCode);
        return account;
    }

    @Override
    public void save(Account account) {
        log.debug("Persisting account {} in MongoDB", account.id().id());
        mongoDataAccountRepository.findById(accountMapper.toObjectId(account.id().id()))
                .ifPresentOrElse(
                        document -> updateExistingDocument(document, account),
                        () -> mongoDataAccountRepository.save(accountMapper.toDocument(account))
                );
        log.debug("MongoDB persisted account {}", account.id().id());
    }

    private void updateExistingDocument(AccountDocument document, Account account) {
        document.setEmail(account.email().email());
        document.setEncodedPassword(account.encodedPassword().value());
        document.setRole(account.role().name());
        document.setAccountStatus(account.status().name());
        document.setVerified(account.isVerified());
        document.setPublicCode(account.publicCode().value());
        document.setEncodedLockerPassword(account.lockerPassword().value());
        document.setLastLoginAt(toIsoString(account.lastLoginAt()));
        mongoDataAccountRepository.save(document);
    }

    private String toIsoString(Date date) {
        return date == null ? null : date.value().toString();
    }
}
