package com.luispiquinrey.backend.account.domain;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String id) {
        super("account not found: " + id);
    }
}
