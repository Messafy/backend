package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.domain.model.*;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StompJwtAuthenticationInterceptorTest {
    private static final String KEY = "synthetic-test-key-with-at-least-thirty-two-characters";

    @Test
    @Tag("stompAuthentication")
    @Timeout(1)
    void rejectsTokenForDeletedAccount() {
        Account account = account();
        String token = new SpringJwtTokenIssuer(KEY).issue(account);
        account.delete();
        var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + token);
        accessor.setLeaveMutable(true);

        assertThrows(MessagingException.class, () -> interceptor(account)
                .preSend(MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders()), null));
    }

    @Test
    @Tag("stompAuthorization")
    @Timeout(1)
    void rejectsWildcardAndDirectBrokerSubscriptions() {
        var interceptor = interceptor(account());
        for (String destination : new String[]{"/queue/**", "/queue/notes-userVictim", "/user/other/queue/notes", "/user/**"}) {
            assertThrows(MessagingException.class,
                    () -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, destination, user()), null));
        }
    }

    @Test
    @Tag("stompAuthorization")
    @Timeout(1)
    void rejectsClientPublishingAndAnonymousSubscriptions() {
        var interceptor = interceptor(account());
        assertThrows(MessagingException.class,
                () -> interceptor.preSend(frame(StompCommand.SEND, "/queue/notes-userVictim", user()), null));
        assertThrows(MessagingException.class,
                () -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/user/queue/notes", null), null));
    }

    @Test
    @Tag("stompAuthorization")
    @Timeout(1)
    void allowsOnlyTheAuthenticatedUsersNotificationSubscription() {
        var frame = frame(StompCommand.SUBSCRIBE, "/user/queue/notes", user());
        assertSame(frame, interceptor(account()).preSend(frame, null));
    }

    @Test
    @Tag("stompAuthentication")
    @Timeout(1)
    void authenticatesConnectAndUsesAccountIdAsTheUserDestinationPrincipal() {
        Account account = account();
        var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + new SpringJwtTokenIssuer(KEY).issue(account));
        accessor.setLeaveMutable(true);
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertSame(message, interceptor(account).preSend(message, null));
        assertEquals(account.id().id(), accessor.getUser().getName());
        assertTrue(((org.springframework.security.core.Authentication) accessor.getUser()).isAuthenticated());
    }

    @Test
    @Tag("stompAuthorization")
    @Timeout(1)
    void permitsProtocolCleanupAndHeartbeatsButRejectsUnknownFrames() {
        var interceptor = interceptor(account());
        var unsubscribe = frame(StompCommand.UNSUBSCRIBE, null, user());
        var disconnect = frame(StompCommand.DISCONNECT, null, null);
        var heartbeat = StompHeaderAccessor.createForHeartbeat();
        var heartbeatMessage = MessageBuilder.createMessage(new byte[0], heartbeat.getMessageHeaders());

        assertSame(unsubscribe, interceptor.preSend(unsubscribe, null));
        assertSame(disconnect, interceptor.preSend(disconnect, null));
        assertSame(heartbeatMessage, interceptor.preSend(heartbeatMessage, null));
        assertThrows(MessagingException.class, () -> interceptor.preSend(MessageBuilder.withPayload(new byte[0]).build(), null));
    }

    @Test
    @Tag("stompAuthorization")
    @Timeout(1)
    void deniedWildcardSubscriptionCannotReceiveAnotherSessionsBrokerMessages() {
        var inbound = new ExecutorSubscribableChannel();
        var outbound = new ExecutorSubscribableChannel();
        var brokerChannel = new ExecutorSubscribableChannel();
        var broker = new SimpleBrokerMessageHandler(inbound, outbound, brokerChannel, List.of("/queue"));
        var delivered = new ArrayList<Message<?>>();
        outbound.subscribe(message -> delivered.add(message));
        Account account = account();
        inbound.addInterceptor(interceptor(account));
        broker.start();
        try {
            var connect = StompHeaderAccessor.create(StompCommand.CONNECT);
            connect.setSessionId("test-session");
            connect.setNativeHeader("Authorization", "Bearer " + new SpringJwtTokenIssuer(KEY).issue(account));
            connect.setLeaveMutable(true);
            inbound.send(MessageBuilder.createMessage(new byte[0], connect.getMessageHeaders()));
            assertThrows(MessagingException.class, () -> inbound.send(
                    frame(StompCommand.SUBSCRIBE, "/queue/**", connect.getUser())));
            assertThrows(MessagingException.class, () -> inbound.send(
                    frame(StompCommand.SEND, "/queue/notes-userVictim", connect.getUser())));
            delivered.clear();
            var headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headers.setDestination("/queue/notes-userVictim");

            brokerChannel.send(MessageBuilder.createMessage(new byte[]{1}, headers.getMessageHeaders()));

            assertTrue(delivered.isEmpty());
        } finally {
            broker.stop();
        }
    }

    private Principal user() {
        return new UsernamePasswordAuthenticationToken("account-id", null,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
    }

    private Message<byte[]> frame(StompCommand command, String destination, Principal user) {
        var accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        accessor.setSessionId("test-session");
        accessor.setSubscriptionId("test-subscription");
        accessor.setUser(user);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private StompJwtAuthenticationInterceptor interceptor(Account account) {
        return new StompJwtAuthenticationInterceptor(new SpringJwtTokenVerifier(KEY), email -> new AccountUserDetails(account));
    }

    private Account account() {
        return new Account.AccountBuilder().id(new UserId("507f1f77bcf86cd799439011"))
                .email(new Email("user@example.test")).encodedPassword(new EncodedPassword("encoded-for-test"))
                .lockerPassword(new LockerPassword("encoded-locker-for-test"))
                .publicCode(new PublicCode("V1StGXR8_Z5j")).isVerified(true).build();
    }
}
