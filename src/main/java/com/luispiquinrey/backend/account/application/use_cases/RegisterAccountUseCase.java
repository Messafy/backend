package com.luispiquinrey.backend.account.application.use_cases;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.account.application.dto.request.RegisterAccountRequest;
import com.luispiquinrey.backend.account.application.port.AccountIdGenerator;
import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.application.port.PublicCodeGenerator;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.exception.AccountConflictException;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.events.DomainEventPublisher;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;

@LogExecution
public class RegisterAccountUseCase {

    private final Logger log;

    private final AccountRepository accountRepository;
    private final PasswordHasher passwordHasher;
    private final DomainEventPublisher domainEventPublisher;
    private final AccountIdGenerator accountIdGenerator;
    private final PublicCodeGenerator publicCodeGenerator;

    public RegisterAccountUseCase(
            AccountRepository accountRepository,
            PasswordHasher passwordHasher,
            DomainEventPublisher domainEventPublisher,
            AccountIdGenerator accountIdGenerator,
            PublicCodeGenerator publicCodeGenerator,
            Logger log
    ) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
        this.domainEventPublisher = domainEventPublisher;
        this.accountIdGenerator = accountIdGenerator;
        this.publicCodeGenerator = publicCodeGenerator;
        this.log = log;
    }

    public Result<AccountResponse> register(RegisterAccountRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("register request cannot be null"));
        }

        log.debug("Registering account for {}", request.email());

        EncodedPassword encodedPassword = passwordHasher.hash(request.rawPassword());
        EncodedPassword encodedLockerPassword = passwordHasher.hash(request.rawLockerPassword());
        Account account = new Account.AccountBuilder()
                .id(new UserId(accountIdGenerator.nextId()))
                .email(request.toEmail())
                .encodedPassword(encodedPassword)
                .publicCode(new PublicCode(publicCodeGenerator.nextCode()))
                .lockerPassword(new LockerPassword(encodedLockerPassword.value()))
                .build();

        try {
            accountRepository.save(account);
        } catch (AccountConflictException e) {
            return Result.failure(ApplicationError.conflictError(e.getMessage()));
        }

        log.debug("Registered account {}", account.id().id());

        domainEventPublisher.publish(new AccountRegistered(account.id().id(), account.email().email()));

        return Result.success(AccountResponse.from(account));
    }
}
