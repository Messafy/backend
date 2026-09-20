package com.luispiquinrey.backend.account.application.port;

import com.luispiquinrey.backend.account.domain.model.Account;

public interface TokenIssuer {

    String issue(Account account);
}
