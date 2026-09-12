package com.luispiquinrey.backend.notes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.luispiquinrey.backend.share.identity.UserId;

class SharedWithTest {

    private static final String VALID_ID = "507f1f77bcf86cd799439012";

    @Test
    @Tag("sharedWith")
    void shouldCreateFromValidUserId() {
        UserId userId = new UserId(VALID_ID);
        SharedWith sharedWith = new SharedWith(userId);

        assertEquals(userId, sharedWith.ownerId());
    }

    @Test
    @Tag("sharedWith")
    void shouldCreateFromValidString() {
        SharedWith sharedWith = new SharedWith(VALID_ID);

        assertEquals(VALID_ID, sharedWith.ownerId().id());
    }

    @Test
    @Tag("sharedWith")
    void shouldThrowWhenOwnerIdIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new SharedWith((UserId) null)
        );
        assertTrue(ex.getMessage().contains("sharedWith cannot be null"));
    }

    @Test
    @Tag("sharedWith")
    void shouldThrowWhenStringIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new SharedWith(""));
    }

    @Test
    @Tag("sharedWith")
    void shouldThrowWhenStringIsInvalidObjectId() {
        assertThrows(IllegalArgumentException.class, () -> new SharedWith("not-a-valid-id"));
    }

    @Test
    @Tag("sharedWith")
    void shouldEqualWhenSameUserId() {
        SharedWith a = new SharedWith(VALID_ID);
        SharedWith b = new SharedWith(VALID_ID);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @Tag("sharedWith")
    void shouldNotEqualWhenDifferentUserId() {
        SharedWith a = new SharedWith(VALID_ID);
        SharedWith b = new SharedWith("507f1f77bcf86cd799439013");

        assertNotEquals(a, b);
    }
}
