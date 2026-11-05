package com.luispiquinrey.backend.account.application.use_cases;

import com.luispiquinrey.backend.account.api.VerificationEmailRequested;
import com.luispiquinrey.backend.account.application.dto.request.ResendVerificationRequest;
import com.luispiquinrey.backend.account.application.dto.response.ResendVerificationResponse;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.events.DomainEventPublisher;
import com.luispiquinrey.backend.share.logging.LogExecution;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.share.result.Result;

@LogExecution
public class ResendVerificationUseCase {

    private final Logger log;
    private final AccountRepository accountRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ResendVerificationUseCase(
            AccountRepository accountRepository,
            DomainEventPublisher domainEventPublisher,
            Logger log
    ) {
        this.accountRepository = accountRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.log = log;
    }

    public Result<ResendVerificationResponse> resend(ResendVerificationRequest request) {
        if (request == null) {
            return Result.failure(ApplicationError.validationError("resend verification request cannot be null"));
        }
        accountRepository.findByEmail(request.email())
                .filter(account -> account.isActive() && !account.isVerified())
                .ifPresentOrElse(
                        this::requestEmail,
                        () -> log.debug("Skipped verification email because no pending account matched")
                );
        return Result.success(ResendVerificationResponse.acceptedResponse());
    }

    private void requestEmail(Account account) {
        log.debug("Requesting a new verification email for account {}", account.id().id());
        domainEventPublisher.publish(new VerificationEmailRequested(account.id().id(), account.email().email()));
    }
}
