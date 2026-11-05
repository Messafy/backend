package com.luispiquinrey.backend.account.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenVerifier jwtTokenVerifier;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenVerifier jwtTokenVerifier,
            UserDetailsService userDetailsService
    ) {
        this.jwtTokenVerifier = jwtTokenVerifier;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = extractJwt(request);

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authenticate(jwt, request);
        } catch (IllegalArgumentException | AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            log.debug("JWT authentication failed");
            SecurityProblemResponses.write(response, HttpStatus.UNAUTHORIZED, "invalid bearer token");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String extractJwt(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return header.substring(BEARER_PREFIX.length());
    }

    private void authenticate(String jwt, HttpServletRequest request) {
        if (isAlreadyAuthenticated()) {
            return;
        }

        String userEmail = jwtTokenVerifier.verifyAccessToken(jwt)
                .orElseThrow(() -> new BadCredentialsException("invalid bearer token"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        new AccountStatusUserDetailsChecker().check(userDetails);

        setAuthentication(userDetails, request);
        log.debug("Authenticated user {} via JWT", userDetails.getUsername());
    }

    private boolean isAlreadyAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
