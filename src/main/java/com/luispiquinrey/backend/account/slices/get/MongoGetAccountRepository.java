package com.luispiquinrey.backend.account.slices.get;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.infrastructure.AccountMapper;
import com.luispiquinrey.backend.account.infrastructure.MongoDataAccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoGetAccountRepository implements GetAccountRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoGetAccountRepository.class);

    private final AccountMapper mapper;
    private final MongoDataAccountRepository repository;

    public MongoGetAccountRepository(AccountMapper mapper, MongoDataAccountRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Optional<Account> findById(UserId id) {
        log.debug("Querying MongoDB for account {}", id.id());
        Optional<Account> account = repository.findById(mapper.toObjectId(id.id()))
                .map(mapper::toDomain);
        log.debug("MongoDB {} account {}", account.isPresent() ? "found" : "did not find", id.id());
        return account;
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        log.debug("Querying MongoDB for account with email {}", email);
        Optional<Account> account = repository.findByEmail(email)
                .map(mapper::toDomain);
        log.debug("MongoDB {} account with email {}", account.isPresent() ? "found" : "did not find", email);
        return account;
    }
}
