package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.domain.model.*;
import com.luispiquinrey.backend.share.identity.UserId;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationRegressionTest {
    private static final String KEY = "synthetic-test-key-with-at-least-thirty-two-characters";

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Tag("jwtAuthenticationRegression")
    @Timeout(1)
    void rejectsPreviouslyIssuedTokenWhenAccountHasBeenDeleted() throws Exception {
        Account account = account();
        String token = new SpringJwtTokenIssuer(KEY).issue(account);
        account.delete();
        var details = new AccountUserDetails(account);
        var verifier = new SpringJwtTokenVerifier(KEY);
        var filter = new JwtAuthenticationFilter(verifier, email -> details);
        var request = request(token);
        var response = new MockHttpServletResponse();
        var reached = new AtomicBoolean();

        filter.doFilter(request, response, (req, res) -> reached.set(true));

        assertFalse(verifier.isTokenValid(token, details));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(401, response.getStatus());
        assertFalse(reached.get());
    }

    @Test
    @Tag("jwtAuthenticationRegression")
    @Timeout(1)
    void malformedTokenAlwaysProducesUnauthorizedEvenWhenNoMvcHandlerResolvesIt() throws Exception {
        var filter = new JwtAuthenticationFilter(new SpringJwtTokenVerifier(KEY),
                email -> new AccountUserDetails(account()));
        var response = new MockHttpServletResponse();
        var reached = new AtomicBoolean();

        filter.doFilter(request("malformed"), response, (req, res) -> reached.set(true));

        assertEquals(401, response.getStatus());
        assertFalse(reached.get());
    }

    @Test
    @Tag("jwtAuthenticationRegression")
    @Timeout(1)
    void doesNotSwallowFailuresFromTheApplicationFilterChain() {
        Account account = account();
        var filter = new JwtAuthenticationFilter(new SpringJwtTokenVerifier(KEY),
                email -> new AccountUserDetails(account));
        var failure = new ServletException("synthetic application failure");

        assertSame(failure, assertThrows(ServletException.class, () -> filter.doFilter(
                request(new SpringJwtTokenIssuer(KEY).issue(account)), new MockHttpServletResponse(),
                (req, res) -> { throw failure; })));
    }

    private MockHttpServletRequest request(String token) {
        var request = new MockHttpServletRequest("GET", "/v1/accounts/me");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private Account account() {
        return new Account.AccountBuilder().id(new UserId("507f1f77bcf86cd799439011"))
                .email(new Email("user@example.test")).encodedPassword(new EncodedPassword("encoded-for-test"))
                .lockerPassword(new LockerPassword("encoded-locker-for-test"))
                .publicCode(new PublicCode("V1StGXR8_Z5j")).isVerified(true).build();
    }
}
