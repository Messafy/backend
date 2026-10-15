package com.luispiquinrey.backend.account.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class RateLimiterFilterTest {

    @Test
    @Timeout(1)
    @Tag("rateLimiterFilter")
    void shouldRejectRequestsAfterTenTokensAreConsumed() throws Exception {
        RateLimiterFilter filter = new RateLimiterFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.1");
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 11; i++) {
            filter.doFilter(request, response, chain);
        }

        verify(chain, org.mockito.Mockito.times(10)).doFilter(request, response);
        verifyNoMoreInteractions(chain);
        assertEquals(429, response.getStatus());
        assertEquals("1", response.getHeader("Retry-After"));
        assertEquals("application/problem+json", response.getContentType());
    }
}
