package com.luispiquinrey.backend.account.infrastructure.config;

import com.luispiquinrey.backend.account.application.service.GetAccountService;
import com.luispiquinrey.backend.account.application.service.LoginAccountService;
import com.luispiquinrey.backend.account.application.service.RegisterAccountService;
import com.luispiquinrey.backend.account.application.service.VerifyAccountService;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.account.infrastructure.security.AccountUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@Import({GetAccountService.class, LoginAccountService.class, RegisterAccountService.class, VerifyAccountService.class})
public class AccountConfig {

    @Bean
    @Profile("!test")
    UserDetailsService userDetailsService(AccountRepository repository) {
        return new AccountUserDetailsService(repository);
    }
}
