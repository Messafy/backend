package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.application.dto.AccountView;
import com.luispiquinrey.backend.account.application.dto.RegisterAccountCommand;
import com.luispiquinrey.backend.account.application.service.RegisterAccountService;
import com.luispiquinrey.backend.account.presentation.rest.RegisterAccountController;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegisterAccountController.class)
@Import(TestSecurityConfig.class)
class AccountCsrfCorsTest {

    private static final String FRONTEND_ORIGIN = "http://localhost:5173";
    private static final String UNTRUSTED_ORIGIN = "http://evil.example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterAccountService service;

    @Test
    @Timeout(1)
    @Tag("accountCsrfCors")
    void shouldProcessMutatingRequestWithoutCsrfToken() throws Exception {
        when(service.register(any(RegisterAccountCommand.class))).thenReturn(accountView());

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"rawPassword\":\"Password1!\",\"rawLockerPassword\":\"Locker1!\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @Timeout(1)
    @Tag("accountCsrfCors")
    void shouldAnswerPreflightFromFrontendOrigin() throws Exception {
        mockMvc.perform(options("/v1/auth/register")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).contains("POST"),
                        "preflight should allow POST"));
    }

    @Test
    @Timeout(1)
    @Tag("accountCsrfCors")
    void shouldRejectPreflightFromUntrustedOrigin() throws Exception {
        mockMvc.perform(options("/v1/auth/register")
                        .header(HttpHeaders.ORIGIN, UNTRUSTED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    private AccountView accountView() {
        return new AccountView(
                "507f1f77bcf86cd799439011",
                "user@example.com",
                "V1StGXR8_Z5j",
                "USER",
                "ACTIVE",
                false,
                Instant.parse("2026-09-12T10:15:30Z"),
                null
        );
    }
}
