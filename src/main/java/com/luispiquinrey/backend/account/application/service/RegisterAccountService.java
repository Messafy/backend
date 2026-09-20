package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.dto.RegisterAccountCommand;
import com.luispiquinrey.backend.account.application.port.AccountIdGenerator;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.events.DomainEventPublisher;
import com.luispiquinrey.backend.share.identity.UserId;
import org.springframework.stereotype.Service;

@Service
public class RegisterAccountService {

    private final AccountRepository accountRepository;
    private final PasswordHasher passwordHasher;
    private final DomainEventPublisher domainEventPublisher;
    private final AccountIdGenerator accountIdGenerator;

    public RegisterAccountService(
            AccountRepository accountRepository,
            PasswordHasher passwordHasher,
            DomainEventPublisher domainEventPublisher,
            AccountIdGenerator accountIdGenerator
    ) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
        this.domainEventPublisher = domainEventPublisher;
        this.accountIdGenerator = accountIdGenerator;
    }

    public AccountView register(RegisterAccountCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("register command cannot be null");
        }

        if (command.rawPassword() == null || command.rawPassword().isBlank()) {
            throw new IllegalArgumentException("raw password cannot be blank");
        }

        EncodedPassword encodedPassword = passwordHasher.hash(command.rawPassword());
        Account account = new Account.AccountBuilder()
                .id(new UserId(accountIdGenerator.nextId()))
                .email(new Email(command.email()))
                .encodedPassword(encodedPassword)
                .build();

        accountRepository.save(account);

        domainEventPublisher.publish(new AccountRegistered(account.id().id()));

        return AccountView.from(account);
    }
}
