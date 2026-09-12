package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.Email;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
public interface LoginAccountRepository {
    Optional<Account> byEmail(String email);

    void save(Account account);
}
