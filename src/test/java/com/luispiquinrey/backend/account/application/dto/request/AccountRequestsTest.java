package com.luispiquinrey.backend.account.application.dto.request;

import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class AccountRequestsTest {
    @Test
    @Tag("accountRequests")
    @Timeout(1)
    void convertsValidatedFieldsToDomainValues() {
        var register = new RegisterAccountRequest("USER@EXAMPLE.COM", "Password1!", "Locker1!");
        var login = new LoginAccountRequest("USER@EXAMPLE.COM", "old-password");
        var get = new GetAccountRequest("opaque-id");

        assertEquals("user@example.com", register.email());
        assertEquals(new Email("user@example.com"), register.toEmail());
        assertEquals(new Email("user@example.com"), login.toEmail());
        assertEquals(new UserId("opaque-id"), get.toUserId());
    }

    @Test
    @Tag("accountRequests")
    @Timeout(1)
    void rejectsInvalidEmailBeforeUseCaseExecution() {
        for (String email : new String[]{null, "", " ", "invalid", "user@example.com\r\nFORGED"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RegisterAccountRequest(email, "Password1!", "Locker1!"));
            assertThrows(IllegalArgumentException.class,
                    () -> new LoginAccountRequest(email, "password"));
        }
    }

    @Test
    @Tag("accountRequests")
    @Timeout(1)
    void rejectsInvalidRegistrationPasswords() {
        for (String password : new String[]{null, "", " ", "Pass1!", "a".repeat(127) + "1!", "Password!", "Password1"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RegisterAccountRequest("user@example.com", password, "Locker1!"));
        }
    }

    @Test
    @Tag("accountRequests")
    @Timeout(1)
    void acceptsRegistrationPasswordLengthBoundaries() {
        assertDoesNotThrow(() -> new RegisterAccountRequest("user@example.com", "Passwo1!", "Locker1!"));
        assertDoesNotThrow(() -> new RegisterAccountRequest("user@example.com", "a".repeat(126) + "1!", "a".repeat(64)));
    }

    @Test
    @Tag("accountRequests")
    @Timeout(1)
    void rejectsInvalidLockerPasswords() {
        for (String password : new String[]{null, "", "short", "a".repeat(65), "Locker 1!", "Locker1é"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RegisterAccountRequest("user@example.com", "Password1!", password));
        }
    }

    @Test
    @Tag("accountRequests")
    @Timeout(1)
    void loginValidatesShapeWithoutImposingRegistrationStrength() {
        for (String password : new String[]{null, "", " \t", "a".repeat(129)}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new LoginAccountRequest("user@example.com", password));
        }
        assertDoesNotThrow(() -> new LoginAccountRequest("user@example.com", "old"));
        assertDoesNotThrow(() -> new LoginAccountRequest("user@example.com", "a".repeat(128)));
    }

    @Test
    @Tag("accountRequests")
    @Timeout(1)
    void rejectsMissingIdentifiersAndTokens() {
        for (String value : new String[]{null, "", " \t"}) {
            assertThrows(IllegalArgumentException.class, () -> new GetAccountRequest(value));
            assertThrows(IllegalArgumentException.class, () -> new VerifyAccountRequest(value));
        }
        assertEquals("opaque-token", new VerifyAccountRequest("opaque-token").token());
    }

    @Test
    @Tag("accountRequests")
    @Timeout(1)
    void doesNotExposeCredentialsInStringRepresentation() {
        assertEquals("RegisterAccountRequest[credentials=REDACTED]",
                new RegisterAccountRequest("user@example.com", "Password1!", "Locker1!").toString());
        assertEquals("LoginAccountRequest[credentials=REDACTED]",
                new LoginAccountRequest("user@example.com", "password").toString());
        assertEquals("VerifyAccountRequest[token=REDACTED]", new VerifyAccountRequest("opaque-token").toString());
    }
}
