package com.luispiquinrey.backend.account.application.use_cases;

import com.luispiquinrey.backend.account.api.VerificationEmailRequested;
import com.luispiquinrey.backend.account.application.dto.request.ResendVerificationRequest;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.events.DomainEventPublisher;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResendVerificationUseCaseTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";
    private static final String EMAIL = "user@example.com";

    @Mock
    private AccountRepository repository;

    @Mock
    private DomainEventPublisher publisher;

    @Mock
    private Logger log;

    @InjectMocks
    private ResendVerificationUseCase useCase;

    @Test
    @Timeout(1)
    @Tag("resendVerificationUseCase")
    void requestsAnEmailForActiveUnverifiedAccounts() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(account()));

        var result = useCase.resend(new ResendVerificationRequest("USER@example.com"));

        assertTrue(result.getValue().accepted());
        verify(publisher).publish(new VerificationEmailRequested(ACCOUNT_ID, EMAIL));
    }

    @Test
    @Timeout(1)
    @Tag("resendVerificationUseCase")
    void answersTheSameWithoutSendingForUnknownEmails() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertTrue(useCase.resend(new ResendVerificationRequest(EMAIL)).getValue().accepted());
        verifyNoInteractions(publisher);
    }

    @Test
    @Timeout(1)
    @Tag("resendVerificationUseCase")
    void answersTheSameWithoutSendingForVerifiedAccounts() {
        Account account = account();
        account.verify();
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(account));

        assertTrue(useCase.resend(new ResendVerificationRequest(EMAIL)).getValue().accepted());
        verifyNoInteractions(publisher);
    }

    @Test
    @Timeout(1)
    @Tag("resendVerificationUseCase")
    void answersTheSameWithoutSendingForDeletedAccounts() {
        Account account = account();
        account.delete();
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(account));

        assertTrue(useCase.resend(new ResendVerificationRequest(EMAIL)).getValue().accepted());
        verifyNoInteractions(publisher);
    }

    @Test
    @Timeout(1)
    @Tag("resendVerificationUseCase")
    void rejectsNullAndMalformedRequests() {
        assertTrue(useCase.resend(null).isFailure());
        assertThrows(IllegalArgumentException.class, () -> new ResendVerificationRequest("not-an-email"));
        verifyNoInteractions(repository, publisher);
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"))
                .publicCode(new PublicCode("V1StGXR8_Z5j"))
                .lockerPassword(new LockerPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e"))
                .build();
    }
}
