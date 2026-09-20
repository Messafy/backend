package com.luispiquinrey.backend.account.domain.exception;

public class AccountAuthenticationException extends RuntimeException {

    public AccountAuthenticationException(String message) {
        super(message);
    }
}
