package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.model.Role;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-with-at-least-thirty-two-characters";
    private static final String OTHER_SECRET = "another-test-key-with-at-least-thirty-two-characters";
    private static final String EMAIL = "user@example.com";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Timeout(1)
    @Tag("jwtAuthenticationFilter")
    void shouldAuthenticateRequestsWithValidBearerTokens() throws Exception {
        AccountUserDetails userDetails = new AccountUserDetails(account());
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new SpringJwtTokenVerifier(SECRET),
                userDetailsService
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + new SpringJwtTokenIssuer(SECRET).issue(account()));
        AtomicReference<Authentication> authentication = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(), (ignoredRequest, ignoredResponse) ->
                authentication.set(SecurityContextHolder.getContext().getAuthentication())
        );

        assertSame(userDetails, authentication.get().getPrincipal());
        assertEquals("ROLE_USER", authentication.get().getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @Timeout(1)
    @Tag("jwtAuthenticationFilter")
    void shouldNotAuthenticateRequestsWithoutBearerTokens() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new SpringJwtTokenVerifier(SECRET),
                mock(UserDetailsService.class)
        );
        AtomicReference<Authentication> authentication = new AtomicReference<>();

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (ignoredRequest, ignoredResponse) ->
                authentication.set(SecurityContextHolder.getContext().getAuthentication())
        );

        assertNull(authentication.get());
    }

    @Test
    @Timeout(1)
    @Tag("jwtAuthenticationFilter")
    void shouldRejectInvalidBearerTokensWithoutAuthenticating() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new SpringJwtTokenVerifier(SECRET),
                mock(UserDetailsService.class)
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenSignedWithAnotherKey());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> { });

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private String tokenSignedWithAnotherKey() {
        return Jwts.builder()
                .claim("email", EMAIL)
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(OTHER_SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId("507f1f77bcf86cd799439011"))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"))
                .publicCode(new PublicCode("V1StGXR8_Z5j"))
                .lockerPassword(new LockerPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e"))
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .isVerified(true)
                .createdAt(new Date(Instant.parse("2026-01-01T10:00:00Z")))
                .lastLoginAt(new Date(Instant.parse("2026-01-02T10:00:00Z")))
                .build();
    }
}
