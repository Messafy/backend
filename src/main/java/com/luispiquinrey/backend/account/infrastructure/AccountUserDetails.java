package com.luispiquinrey.backend.account.infrastructure;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AccountUserDetails implements UserDetails, AuthenticatedUser {

    private final Account account;

    public AccountUserDetails(Account account) {
        this.account = account;
    }

    public Account account() {
        return account;
    }

    @Override
    public String accountId() {
        return account.id().id();
    }

    @Override
    public String getUsername() {
        return account.email().email();
    }

    @Override
    public String getPassword() {
        return account.encodedPassword().value();
    }

    @Override
    public Collection<? extends GrantedAuthority>
    getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" +
                account.role().name()));
    }

    @Override
    public boolean isEnabled() {
        return account.isActive();
    }
}
