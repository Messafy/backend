package com.luispiquinrey.backend.account.infrastructure.config;

import com.luispiquinrey.backend.account.application.use_cases.GetAccountUseCase;
import com.luispiquinrey.backend.account.application.use_cases.LoginAccountUseCase;
import com.luispiquinrey.backend.account.application.use_cases.RegisterAccountUseCase;
import com.luispiquinrey.backend.account.application.use_cases.ResendVerificationUseCase;
import com.luispiquinrey.backend.account.application.use_cases.VerifyAccountUseCase;
import com.luispiquinrey.backend.account.presentation.presenter.GetAccountPresenter;
import com.luispiquinrey.backend.account.presentation.presenter.RegisterAccountPresenter;
import com.luispiquinrey.backend.account.presentation.presenter.ResendVerificationPresenter;
import com.luispiquinrey.backend.account.presentation.presenter.LoginAccountPresenter;
import com.luispiquinrey.backend.account.presentation.presenter.VerifyAccountPresenter;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.account.infrastructure.security.AccountUserDetailsService;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Duration;

@Configuration
@Import({GetAccountUseCase.class, LoginAccountUseCase.class, RegisterAccountUseCase.class, VerifyAccountUseCase.class,
        ResendVerificationUseCase.class, ResendVerificationPresenter.class,
        GetAccountPresenter.class, RegisterAccountPresenter.class, LoginAccountPresenter.class, VerifyAccountPresenter.class,
        HttpResponsePresenter.class})
public class AccountConfig {

    @Bean
    @Profile("!test")
    UserDetailsService userDetailsService(
            AccountRepository repository,
            @Value("${security.account-cache.ttl:PT30S}") Duration accountCacheTtl
    ) {
        return new AccountUserDetailsService(repository, accountCacheTtl);
    }
}
