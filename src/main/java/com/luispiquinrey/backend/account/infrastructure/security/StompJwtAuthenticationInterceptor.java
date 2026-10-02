package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class StompJwtAuthenticationInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenVerifier jwtTokenVerifier;
    private final UserDetailsService userDetailsService;

    public StompJwtAuthenticationInterceptor(
            JwtTokenVerifier jwtTokenVerifier,
            UserDetailsService userDetailsService
    ) {
        this.jwtTokenVerifier = jwtTokenVerifier;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = extractToken(accessor.getFirstNativeHeader(AUTHORIZATION_HEADER));
        String email = jwtTokenVerifier.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtTokenVerifier.isTokenValid(token, userDetails)
                || !(userDetails instanceof AuthenticatedUser authenticatedUser)) {
            throw new MessagingException("invalid WebSocket authentication");
        }

        accessor.setUser(new UsernamePasswordAuthenticationToken(
                authenticatedUser.accountId(),
                null,
                userDetails.getAuthorities()
        ));
        return message;
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("missing WebSocket bearer token");
        }
        return authorization.substring(BEARER_PREFIX.length());
    }
}
