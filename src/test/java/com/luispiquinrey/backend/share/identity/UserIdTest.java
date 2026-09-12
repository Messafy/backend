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
        assertTrue(ex.getMessage().contains("id cannot be empty"));
    }

    @Test
    @Tag("userId")
    void shouldThrowWhenIdIsTooShort() {
        assertThrows(IllegalArgumentException.class, () -> new UserId("507f1f77bcf86cd79943901"));
    }

    @Test
    @Tag("userId")
    void shouldThrowWhenIdIsTooLong() {
        assertThrows(IllegalArgumentException.class, () -> new UserId("507f1f77bcf86cd7994390123"));
    }

    @Test
    @Tag("userId")
    void shouldThrowWhenIdContainsNonHexCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new UserId("507f1f77bcf86cd79943901z"));
    }

    @Test
    @Tag("userId")
    void shouldThrowWhenIdIsPlainText() {
        assertThrows(IllegalArgumentException.class, () -> new UserId("not-a-valid-id"));
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
