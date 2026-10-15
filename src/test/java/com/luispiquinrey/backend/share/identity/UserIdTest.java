package com.luispiquinrey.backend.share.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class UserIdTest {

    private static final String VALID_ID = "507f1f77bcf86cd799439012";

    @Test
    @Tag("userId")
    void shouldCreateFromValidObjectId() {
        UserId userId = new UserId(VALID_ID);
        assertEquals(VALID_ID, userId.id());
    }

    @Test
    @Tag("userId")
    void shouldThrowWhenIdIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new UserId(null)
        );
        assertTrue(ex.getMessage().contains("id cannot be null"));
    }

    @Test
    @Tag("userId")
    void shouldThrowWhenIdIsEmpty() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new UserId("")
        );
        assertTrue(ex.getMessage().contains("blank"));
    }

    @Test
    @Tag("userId")
    void shouldAcceptShortOpaqueId() {
        assertEquals("user-1", new UserId("user-1").id());
    }

    @Test
    @Tag("userId")
    void shouldAcceptUuid() {
        String uuid = "e8315d38-3156-4d93-b4d2-04fe36305ac4";
        assertEquals(uuid, new UserId(uuid).id());
    }

    @Test
    @Tag("userId")
    void shouldRejectWhitespaceOnly() {
        assertThrows(IllegalArgumentException.class, () -> new UserId(" \t"));
    }

    @Test
    @Tag("userId")
    void shouldAcceptOpaqueId() {
        assertEquals("external-account", new UserId("external-account").id());
    }

    @Test
    @Tag("userId")
    void shouldAcceptUppercaseHex() {
        UserId userId = new UserId("507F1F77BCF86CD799439012");
        assertEquals("507F1F77BCF86CD799439012", userId.id());
    }

    @Test
    @Tag("userId")
    void shouldEqualWhenSameString() {
        UserId a = new UserId(VALID_ID);
        UserId b = new UserId(VALID_ID);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @Tag("userId")
    void shouldNotEqualWhenDifferentStrings() {
        UserId a = new UserId(VALID_ID);
        UserId b = new UserId("507f1f77bcf86cd799439013");
        assertNotEquals(a, b);
    }
}
