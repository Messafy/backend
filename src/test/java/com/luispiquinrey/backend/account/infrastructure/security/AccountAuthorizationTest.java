package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.service.GetAccountService;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.model.Role;
import com.luispiquinrey.backend.account.presentation.rest.GetAccountController;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GetAccountController.class)
@Import(TestSecurityConfig.class)
class AccountAuthorizationTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAccountService service;

    @Test
    @Timeout(1)
    @Tag("accountAuthorization")
    void shouldRejectUnauthenticatedRequestsToCurrentAccount() throws Exception {
        mockMvc.perform(get("/v1/accounts/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Timeout(1)
    @Tag("accountAuthorization")
    void shouldAllowUsersToReadTheirCurrentAccount() throws Exception {
        Account account = account(Role.USER);
        when(service.getMyAccount(account.id())).thenReturn(AccountView.from(account));

        mockMvc.perform(get("/v1/accounts/me").with(user(new AccountUserDetails(account))))
                .andExpect(status().isOk());
    }

    @Test
    @Timeout(1)
    @Tag("accountAuthorization")
    void shouldForbidUsersFromReadingAccountsById() throws Exception {
        mockMvc.perform(get("/v1/accounts/{id}", ACCOUNT_ID)
                        .with(user(new AccountUserDetails(account(Role.USER)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Timeout(1)
    @Tag("accountAuthorization")
    void shouldAllowAdminsToReadAccountsById() throws Exception {
        Account account = account(Role.ADMIN);
        when(service.findById(ACCOUNT_ID)).thenReturn(AccountView.from(account));

        mockMvc.perform(get("/v1/accounts/{id}", ACCOUNT_ID)
                        .with(user(new AccountUserDetails(account))))
                .andExpect(status().isOk());
    }

    private Account account(Role role) {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"))
                .publicCode(new PublicCode("V1StGXR8_Z5j"))
                .lockerPassword(new LockerPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e"))
                .role(role)
                .status(AccountStatus.ACTIVE)
                .isVerified(true)
                .createdAt(new Date(Instant.parse("2026-01-01T10:00:00Z")))
                .lastLoginAt(new Date(Instant.parse("2026-01-02T10:00:00Z")))
                .build();
    }
}
