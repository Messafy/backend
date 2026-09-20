package com.luispiquinrey.backend.account.domain.exception;

public class AccountPasswordMismatchException extends RuntimeException {

    public AccountPasswordMismatchException(String message) {
        super(message);
    }
}
