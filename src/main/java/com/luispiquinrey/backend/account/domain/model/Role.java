package com.luispiquinrey.backend.account.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum Role {
    ADMIN,
    USER;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isUser() {
        return this == USER;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
