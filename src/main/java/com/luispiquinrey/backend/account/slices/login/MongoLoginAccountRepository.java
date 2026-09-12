package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.infrastructure.AccountDocument;
import com.luispiquinrey.backend.account.infrastructure.AccountMapper;
import com.luispiquinrey.backend.account.infrastructure.MongoDataAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoLoginAccountRepository implements LoginAccountRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoLoginAccountRepository.class);

    private final AccountMapper accountMapper;
    private final MongoDataAccountRepository mongoDataAccountRepository;

    public MongoLoginAccountRepository(AccountMapper accountMapper, MongoDataAccountRepository mongoDataAccountRepository) {
        this.accountMapper = accountMapper;
        this.mongoDataAccountRepository = mongoDataAccountRepository;
    }

    @Override
    public Optional<Account> byEmail(String email) {
        log.debug("Querying MongoDB for account {}", email);
        Optional<Account> account = mongoDataAccountRepository.findByEmail(email)
                .map(accountMapper::toDomain);
        log.debug("MongoDB {} account {}", account.isPresent() ? "found" : "did not find", email);
        return account;
    }

    @Override
    public void save(Account account) {
        log.debug("Updating account {} login state in MongoDB", account.id().id());
        AccountDocument document = mongoDataAccountRepository.findById(accountMapper.toObjectId(account.id().id()))
                .orElseThrow(() -> {
                    log.warn("Cannot update login state because account {} was not found", account.id().id());
                    return new UsernameNotFoundException(account.id().id());
                });

        document.setLastLoginAt(account.lastLoginAt().value().toString());
        mongoDataAccountRepository.save(document);
        log.debug("MongoDB updated account {} login state", account.id().id());
    }
}
