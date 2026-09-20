package com.luispiquinrey.backend.account.api;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record AccountRegistered(String accountId) {

    public AccountRegistered {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be null or blank");
        }
    }
}
