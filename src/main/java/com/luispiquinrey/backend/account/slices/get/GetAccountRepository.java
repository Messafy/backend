package com.luispiquinrey.backend.account.slices.get;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.share.identity.UserId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
public interface GetAccountRepository {

    Optional<Account> findById(UserId id);

    Optional<Account> findByEmail(String email);
}
