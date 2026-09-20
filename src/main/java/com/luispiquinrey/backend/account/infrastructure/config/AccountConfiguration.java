package com.luispiquinrey.backend.account.infrastructure.config;

import com.luispiquinrey.backend.account.infrastructure.persistence.AccountMapper;
import com.luispiquinrey.backend.account.infrastructure.persistence.MongoDataAccountRepository;
import com.luispiquinrey.backend.account.infrastructure.security.AccountUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class AccountConfiguration {

    @Bean
    @Profile("!test")
    UserDetailsService userDetailsService(MongoDataAccountRepository repository, AccountMapper mapper) {
        return new AccountUserDetailsService(repository, mapper);
    }
}
