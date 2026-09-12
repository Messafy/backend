package com.luispiquinrey.backend.account.slices.register;

import com.luispiquinrey.backend.account.domain.Account;
import org.jmolecules.ddd.annotation.Repository;

@Repository
public interface RegisterAccountRepository {
    void save(Account account);
}
