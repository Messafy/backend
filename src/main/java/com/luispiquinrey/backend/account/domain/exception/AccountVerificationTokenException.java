package com.luispiquinrey.backend.account.domain.exception;

public class AccountVerificationTokenException extends RuntimeException {

    public AccountVerificationTokenException(String message) {
        super(message);
    }
}
