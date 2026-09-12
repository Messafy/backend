package com.luispiquinrey.backend.account.slices.register;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.infrastructure.AccountMapper;
import com.luispiquinrey.backend.account.infrastructure.MongoDataAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class MongoRegisterAccountRepository implements RegisterAccountRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoRegisterAccountRepository.class);

    private final MongoDataAccountRepository mongoDataAccountRepository;
    private final AccountMapper accountMapper;

    public MongoRegisterAccountRepository(MongoDataAccountRepository mongoDataAccountRepository, AccountMapper accountMapper) {
        this.mongoDataAccountRepository = mongoDataAccountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public void save(Account account) {
        log.debug("Persisting registered account {} in MongoDB", account.id().id());
        mongoDataAccountRepository.save(accountMapper.toDocument(account));
        log.debug("MongoDB persisted registered account {}", account.id().id());
    }
}
