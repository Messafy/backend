package com.luispiquinrey.backend.account.domain.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String id) {
        super("account not found: " + id);
    }
}
