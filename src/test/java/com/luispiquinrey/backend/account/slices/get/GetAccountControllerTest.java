package com.luispiquinrey.backend.account.slices.get;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.AccountStatus;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.account.domain.Role;
import com.luispiquinrey.backend.account.infrastructure.AccountUserDetails;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetAccountControllerTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";

    @Test
    @Timeout(1)
    @Tag("getMyAccountResponse")
    void shouldReturnOkWithCurrentAccount() {
        Account account = account();
        AccountUserDetails principal = new AccountUserDetails(account);
        GetAccountService service = mock(GetAccountService.class);
        GetAccountController controller = new GetAccountController(service);
        when(service.getMyAccount(account.id())).thenReturn(account);

        ResponseEntity<AccountGetResponse> response = controller.me(principal);

        assertAccountResponse(response);
        verify(service).getMyAccount(account.id());
    }

    @Test
    @Timeout(1)
    @Tag("getMyAccountFailurePropagation")
    void shouldPropagateCurrentAccountFailure() {
        Account account = account();
        AccountUserDetails principal = new AccountUserDetails(account);
        GetAccountService service = mock(GetAccountService.class);
        GetAccountController controller = new GetAccountController(service);
        AccountNotFoundException failure = new AccountNotFoundException(ACCOUNT_ID);
        when(service.getMyAccount(account.id())).thenThrow(failure);

        AccountNotFoundException thrown = assertThrows(
                AccountNotFoundException.class,
                () -> controller.me(principal)
        );

        assertSame(failure, thrown);
    }

    @Test
    @Timeout(1)
    @Tag("getAccountByIdResponse")
    void shouldReturnOkWithAccountById() {
        Account account = account();
        AccountGetRequest request = new AccountGetRequest(ACCOUNT_ID);
        GetAccountService service = mock(GetAccountService.class);
        GetAccountController controller = new GetAccountController(service);
        when(service.findById(ACCOUNT_ID)).thenReturn(account);

        ResponseEntity<AccountGetResponse> response = controller.getById(request);

        assertAccountResponse(response);
        verify(service).findById(ACCOUNT_ID);
    }

    @Test
    @Timeout(1)
    @Tag("getAccountByIdFailurePropagation")
    void shouldPropagateGetByIdFailure() {
        AccountGetRequest request = new AccountGetRequest(ACCOUNT_ID);
        GetAccountService service = mock(GetAccountService.class);
        GetAccountController controller = new GetAccountController(service);
        AccountNotFoundException failure = new AccountNotFoundException(ACCOUNT_ID);
        when(service.findById(ACCOUNT_ID)).thenThrow(failure);

        AccountNotFoundException thrown = assertThrows(
                AccountNotFoundException.class,
                () -> controller.getById(request)
        );

        assertSame(failure, thrown);
    }

    private void assertAccountResponse(ResponseEntity<AccountGetResponse> response) {
        AccountGetResponse body = response.getBody();
        assertNotNull(body);
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(ACCOUNT_ID, body.id()),
                () -> assertEquals("user@example.com", body.email()),
                () -> assertEquals("ADMIN", body.role()),
                () -> assertEquals("ACTIVE", body.status()),
                () -> assertEquals(true, body.verified()),
                () -> assertEquals(Instant.parse("2026-01-01T10:00:00Z"), body.createdAt()),
                () -> assertEquals(Instant.parse("2026-01-02T10:00:00Z"), body.lastLoginAt())
        );
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .isVerified(true)
                .createdAt(new Date(Instant.parse("2026-01-01T10:00:00Z")))
                .lastLoginAt(new Date(Instant.parse("2026-01-02T10:00:00Z")))
                .build();
    }
}
