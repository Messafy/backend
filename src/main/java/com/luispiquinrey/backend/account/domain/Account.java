package com.luispiquinrey.backend.account.domain;

import org.jmolecules.ddd.annotation.AggregateRoot;

import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;

import java.io.Serializable;

@AggregateRoot
public class Account implements Serializable {
    private UserId id;
    private Email email;
    private EncodedPassword encodedPassword;
    private Role role;
    private AccountStatus status;
    private boolean isVerified;
    private Date createdAt;
    private Date lastLoginAt;

    public Account(AccountBuilder builder) {
        if (builder.id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (builder.email == null) {
            throw new IllegalArgumentException("email cannot be null");
        }
        if (builder.encodedPassword == null) {
            throw new IllegalArgumentException("encoded password cannot be null");
        }

        this.id = builder.id;
        this.email = builder.email;
        this.encodedPassword = builder.encodedPassword;
        this.role = builder.role == null ? Role.USER : builder.role;
        this.status = builder.status == null ? AccountStatus.ACTIVE : builder.status;
        this.isVerified = builder.isVerified;
        this.createdAt = builder.createdAt == null ? Date.now() : builder.createdAt;
        this.lastLoginAt = builder.lastLoginAt;
    }

    public UserId id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public EncodedPassword encodedPassword() {
        return encodedPassword;
    }

    public Role role() {
        return role;
    }

    public AccountStatus status() {
        return status;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public Date createdAt() {
        return createdAt;
    }

    public Date lastLoginAt() {
        return lastLoginAt;
    }

    public void verify() {
        if (isFinal()) {
            throw new AccountConflictException(
                    "cannot verify an account that is %s".formatted(status)
            );
        }
        this.isVerified = true;
    }

    public void login() {
        // The user must verify the account through the email verification flow before logging in.
        /*
        if (!isVerified) {
            throw new AccountConflictException(
                    "cannot login an account that has not been verified"
            );
        }
         */
        if (isFinal()) {
            throw new AccountConflictException(
                    "cannot login a %s account".formatted(status)
            );
        }
        this.lastLoginAt = Date.now();
    }

    public void delete() {
        transitionTo(AccountStatus.DELETED);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isFinal() {
        return status.isFinal();
    }

    private void transitionTo(AccountStatus nextStatus) {
        if (!status.canTransitionTo(nextStatus)) {
            throw new AccountConflictException(
                    "cannot transition account from %s to %s".formatted(status, nextStatus)
            );
        }
        this.status = nextStatus;
    }

    public static class AccountBuilder {
        private UserId id;
        private Email email;
        private EncodedPassword encodedPassword;
        private Role role;
        private AccountStatus status;
        private boolean isVerified;
        private Date createdAt;
        private Date lastLoginAt;

        public AccountBuilder id(UserId id) {
            this.id = id;
            return this;
        }

        public AccountBuilder email(Email email) {
            this.email = email;
            return this;
        }

        public AccountBuilder encodedPassword(EncodedPassword encodedPassword) {
            this.encodedPassword = encodedPassword;
            return this;
        }

        public AccountBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public AccountBuilder status(AccountStatus status) {
            this.status = status;
            return this;
        }

        public AccountBuilder isVerified(boolean isVerified) {
            this.isVerified = isVerified;
            return this;
        }

        public AccountBuilder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AccountBuilder lastLoginAt(Date lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}
