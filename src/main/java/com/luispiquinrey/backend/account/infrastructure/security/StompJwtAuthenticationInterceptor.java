package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.share.identity.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.messaging.simp.SimpMessageType;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class StompJwtAuthenticationInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompJwtAuthenticationInterceptor.class);

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
        if (accessor == null) {
            throw new MessagingException("invalid WebSocket frame");
        }
        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT || command == StompCommand.STOMP) {
            authenticate(accessor);
            return message;
        }
        if (command == StompCommand.DISCONNECT || accessor.getMessageType() == SimpMessageType.HEARTBEAT) {
            return message;
        }
        if (!(accessor.getUser() instanceof Authentication authentication)
                || !authentication.isAuthenticated() || !hasNotificationRole(authentication)) {
            throw new MessagingException("WebSocket authentication required");
        }
        if (command == StompCommand.UNSUBSCRIBE
                || (command == StompCommand.SUBSCRIBE && "/user/queue/notes".equals(accessor.getDestination()))) {
            return message;
        }
        throw new MessagingException("WebSocket destination or command is not allowed");
    }

    private void authenticate(StompHeaderAccessor accessor) {
        try {
            String token = extractToken(accessor.getFirstNativeHeader(AUTHORIZATION_HEADER));
            String email = jwtTokenVerifier.extractEmail(token);
            if (email == null || email.isBlank()) {
                throw new BadCredentialsException("invalid bearer token");
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            new AccountStatusUserDetailsChecker().check(userDetails);

            if (!jwtTokenVerifier.isTokenValid(token, userDetails)
                    || !(userDetails instanceof AuthenticatedUser authenticatedUser)) {
                throw new MessagingException("invalid WebSocket authentication");
            }

            var authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser.accountId(), null, userDetails.getAuthorities());
            if (!hasNotificationRole(authentication)) {
                throw new MessagingException("WebSocket role is not allowed");
            }
            accessor.setUser(authentication);
            log.debug("Authenticated WebSocket user {}", authenticatedUser.accountId());
        } catch (JwtException | IllegalArgumentException | AuthenticationException exception) {
            log.debug("WebSocket authentication failed");
            throw new MessagingException("invalid WebSocket authentication");
        }
    }

    private boolean hasNotificationRole(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority ->
                "ROLE_USER".equals(authority.getAuthority()) || "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("missing WebSocket bearer token");
        }
        return authorization.substring(BEARER_PREFIX.length());
    }
}
