package com.luispiquinrey.backend.account.infrastructure;

import com.luispiquinrey.backend.account.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AccountUserDetailsService.class);

    private final MongoDataAccountRepository repository;
    private final AccountMapper mapper;

    public AccountUserDetailsService(MongoDataAccountRepository repository, AccountMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user details for {}", email);
        Account account = repository.findByEmail(email)
                .map(mapper::toDomain)
                .orElseThrow(() -> {
                    log.warn("User details lookup failed because account {} was not found", email);
                    return new UsernameNotFoundException(email);
                });
        log.debug("Loaded user details for account {}", account.id().id());
        return new AccountUserDetails(account);
    }
}
