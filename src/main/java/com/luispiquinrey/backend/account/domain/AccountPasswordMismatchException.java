package com.luispiquinrey.backend.account.domain;

public class AccountPasswordMismatchException extends RuntimeException {

    public AccountPasswordMismatchException(String message) {
        super(message);
    }
}
