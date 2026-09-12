package com.luispiquinrey.backend.account.domain;

public class AccountConflictException extends RuntimeException {

    public AccountConflictException(String message) {
        super(message);
    }
}
