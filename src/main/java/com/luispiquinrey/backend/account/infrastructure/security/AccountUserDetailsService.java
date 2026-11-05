package com.luispiquinrey.backend.account.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Duration;

public class AccountUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AccountUserDetailsService.class);
    private static final long MAX_CACHED_ACCOUNTS = 10_000;

    private final AccountRepository repository;
    private final Cache<String, AccountUserDetails> cache;

    public AccountUserDetailsService(AccountRepository repository) {
        this(repository, Duration.ZERO);
    }

    public AccountUserDetailsService(AccountRepository repository, Duration ttl) {
        this.repository = repository;
        this.cache = ttl == null || ttl.isZero() || ttl.isNegative()
                ? null
                : Caffeine.newBuilder().expireAfterWrite(ttl).maximumSize(MAX_CACHED_ACCOUNTS).build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (cache == null) {
            return load(email);
        }
        AccountUserDetails cached = cache.getIfPresent(email);
        if (cached != null) {
            return cached;
        }
        AccountUserDetails loaded = load(email);
        cache.put(email, loaded);
        return loaded;
    }

    private AccountUserDetails load(String email) {
        log.debug("Loading user details for {}", email);
        Account account = repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User details lookup failed because account {} was not found", email);
                    return new UsernameNotFoundException(email);
                });
        log.debug("Loaded user details for account {}", account.id().id());
        return new AccountUserDetails(account);
    }
}
