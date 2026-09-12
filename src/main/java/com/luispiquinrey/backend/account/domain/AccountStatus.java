package com.luispiquinrey.backend.account.domain;

public enum AccountStatus {
    ACTIVE,
    DELETED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isFinal() {
        return this == DELETED;
    }

    public boolean canTransitionTo(AccountStatus nextStatus) {
        if (this == nextStatus) {
            return true;
        }

        if (isFinal()) {
            return false;
        }

        return this == ACTIVE && nextStatus == DELETED;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
