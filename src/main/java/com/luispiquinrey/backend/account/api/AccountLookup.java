package com.luispiquinrey.backend.account.api;

public interface AccountLookup {
    boolean existsActiveAccount(String accountId);
}
