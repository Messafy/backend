package com.luispiquinrey.backend.account.api;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record AccountRegistered(String accountId, String email) {

    public AccountRegistered {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
    }
}
