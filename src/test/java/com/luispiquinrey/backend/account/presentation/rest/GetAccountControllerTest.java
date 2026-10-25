package com.luispiquinrey.backend.account.presentation.rest;

import com.luispiquinrey.backend.account.application.dto.response.AccountResponse;
import com.luispiquinrey.backend.share.result.Result;
import com.luispiquinrey.backend.share.result.ApplicationError;
import com.luispiquinrey.backend.account.application.dto.request.GetAccountRequest;
import com.luispiquinrey.backend.account.application.use_cases.GetAccountUseCase;
import com.luispiquinrey.backend.account.presentation.presenter.GetAccountPresenter;
import com.luispiquinrey.backend.share.presentation.ErrorPresenter;
import com.luispiquinrey.backend.share.presentation.HttpResponsePresenter;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.model.Role;
import com.luispiquinrey.backend.account.infrastructure.security.AccountUserDetails;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import java.time.Instant;

import static de.flapdoodle.embed.mongo.client.AuthenticationSetup.user;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetAccountControllerTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";
    private static final String ENCODED_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final String ENCODED_LOCKER = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e";
    private static final String PUBLIC_CODE = "V1StGXR8_Z5j";

    @Test
    @Timeout(3)
    @Tag("getAccountController")
    void shouldReturnAccountForPathId() throws Exception {
        GetAccountUseCase service = mock(GetAccountUseCase.class);
        when(service.findById(new GetAccountRequest(ACCOUNT_ID))).thenReturn(Result.success(AccountResponse.from(account())));

        mockMvc(service).perform(get("/v1/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.lastLoginAt").value("2026-01-02T10:00:00Z"));
    }

    @Test
    @Timeout(3)
    @Tag("getAccountController")
    void shouldReturnCurrentAccount() throws Exception {
        Account account = account();
        GetAccountUseCase service = mock(GetAccountUseCase.class);
        when(service.getMyAccount(new GetAccountRequest(account.id().id()))).thenReturn(Result.success(AccountResponse.from(account)));

        mockMvc(service).perform(get("/v1/accounts/me").with(user(new AccountUserDetails(account()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID));
    }

    private MockMvc mockMvc(GetAccountUseCase service) {
        return MockMvcBuilders.standaloneSetup(new GetAccountController(
                        service, new GetAccountPresenter(new ErrorPresenter()), new HttpResponsePresenter()))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilter(new SecurityContextHolderFilter(new HttpSessionSecurityContextRepository()))
                .build();
    }

    @Test
    @Timeout(1)
    @Tag("getAccountController")
    void shouldReturnNotFoundForFailedLookupResult() throws Exception {
        GetAccountUseCase service = mock(GetAccountUseCase.class);
        when(service.findById(new GetAccountRequest(ACCOUNT_ID)))
                .thenReturn(Result.failure(ApplicationError.notFoundError("account not found: " + ACCOUNT_ID)));

        mockMvc(service).perform(get("/v1/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("account not found: " + ACCOUNT_ID))
                .andExpect(jsonPath("$.code").value("NOT_FOUND_ERROR"));
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword(ENCODED_PASSWORD))
                .publicCode(new PublicCode(PUBLIC_CODE))
                .lockerPassword(new LockerPassword(ENCODED_LOCKER))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .isVerified(true)
                .createdAt(new Date(Instant.parse("2026-01-01T10:00:00Z")))
                .lastLoginAt(new Date(Instant.parse("2026-01-02T10:00:00Z")))
                .build();
    }
}
