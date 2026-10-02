package com.luispiquinrey.backend.account.domain.repository;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.share.identity.UserId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository {

    Optional<Account> findById(UserId id);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByPublicCode(String publicCode);

    void save(Account account);
}